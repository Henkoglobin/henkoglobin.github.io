# henkoglobin.github.io

## [SoapUI-Scripting mit Groovy](de/2022-04-07_SoapUI-Scripting.md)

Ein großer Vorteil von SoapUI ist seine Erweiterbarkeit durch Groovy-Scripte. Diese können etwa genutzt werden, um Tests zu unterstützen (beispielsweise als Setup- und Teardown-Scripte) oder um das Verhalten von Mocks zu definieren.

In einem unserer Projekte haben wir Groovy-Scripte intensiv genutzt, um automatisiert Testfälle generieren zu können: Da wir bestehende Microservices modernisiert haben, bestand die Anforderung, dass die 'neuen' Services exakt die gleichen Daten liefern sollten wie vor der Modernisierung. Um nun nicht hunderte von Assertions von Hand anlegen zu müssen, haben wir Groovy-Scripte entwickelt, die uns diese Aufgabe abnehmen. 

[Weiterlesen](de/2022-04-07_SoapUI-Scripting.md)