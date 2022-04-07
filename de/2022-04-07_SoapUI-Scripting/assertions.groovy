import org.apache.logging.log4j.core.Logger
import com.eviware.soapui.model.testsuite.TestCaseRunner

// Diese Imports werden wir später noch benötigen
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep
import com.eviware.soapui.impl.wsdl.teststeps.PropertyTransfersTestStep
import com.eviware.soapui.impl.wsdl.teststeps.JdbcRequestTestStep

import groovy.json.JsonSlurper
import groovy.json.JsonException

class AssertionUtilities {
	Logger log
	Object context
	TestCaseRunner testRunner
	JsonSlurper jsonSlurper

	def AssertionUtilities(Logger log, Object context, TestCaseRunner testRunner) {
		this.log = log
		this.context = context
		this.testRunner = testRunner
		
		this.jsonSlurper = new JsonSlurper()
   }

	def createAssertionsForTestCase() {
		for(step in testRunner.testCase.testStepList) {
			if(step.disabled) {
				continue
			}
	
			step.run(testRunner, context)
	
			// Nur für Rest Request Steps wollen wir Assertions generieren
			if(step instanceof RestTestRequestStep) {
				def response = step.httpRequest.response
	
				log.info("[${step.label}] Assertions werden erzeugt...")
				createHttpStatusAssertion(step, response.statusCode)
				createContentAssertions(step, response.responseContent)
				   log.info("[${step.label}] Assertions erfolgreich erzeugt.")
			}
		}
	}

	def createContentAssertions(step, responseContent) {
		try {
			def data = jsonSlurper.parseText(responseContent)
			generateAssertionsForValue(step, "\$", data)
		} catch(JsonException e) {
			testRunner.fail("${step.label} hat kein JSON geliefert; fehlt ein Accept-Header?")
			log.info(e.message)
		}
	}

	def createHttpStatusAssertion(step, statusCode) {
		def name = ":status = $statusCode"
	
		// Die Namen von Assertions müssen innerhalb eines Testschritts immer eindeutig sein; zudem wollen wir verhindern, dass mehrfache Aufrufe unseres Scripts (etwa, nachdem neue Felder hinzugefügt wurden) Duplikate bestehender Assertions anlegen
		if(step.getAssertionByName(name) == null) {
			log.debug("Erstelle $name")
	
			def assertion = step.addAssertion("Valid HTTP Status Codes")
			assertion.name = name
			assertion.codes = statusCode
		} else {
			log.info("Überspringe $name, da sie bereits existiert")
		}
	}

	def generateAssertionsForValue(step, path, value) {
		if(value instanceof List) {
			return generateAssertionsForList(step, path, value)
		} else if(value instanceof Map) {
			return generateAssertionsForMap(step, path, value)
		} else {
			return createJsonPathContentAssertion(step, path, value)
		}
	}

	def generateAssertionsForList(step, path, list) {
		list.eachWithIndex { entry, i ->
			generateAssertionsForValue(step, "$path[$i]", entry)
		}
	}

	def generateAssertionsForMap(step, path, map) {
		for(entry in map) {
			generateAssertionsForValue(step, "$path['${entry.key}']", entry.value)
		}
	}

	def createJsonPathContentAssertion(step, path, value) {
		def name = "$path = $value"

		if(step.getAssertionByName(name) == null) {
			log.debug("Erstelle $name")

			def assertion = step.addAssertion("JsonPath Match")
			assertion.name = name
			assertion.path = path
			assertion.expectedContent = value != null ? value : "null"
		} else {
			log.info("Überspringe $name, da sie bereits existiert")
		}
	}
}

context.assertions = new AssertionUtilities(log, context, testRunner)