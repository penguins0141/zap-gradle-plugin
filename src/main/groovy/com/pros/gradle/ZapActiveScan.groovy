package com.pros.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Executes a ZAP active scan against the applicationUrl. This task will wait until the scan is complete before returning.
 */
class ZapActiveScan extends DefaultTask {
    @TaskAction
    def activeScan() {
        def format = project.zapConfig.reportFormat
        def url = new URL("http://zap/${format}/ascan/action/scan/?zapapiformat=${format}&url=${project.zapConfig.applicationUrl}&recurse=true&inScopeOnly=")

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", project.zapConfig.proxyPort.toInteger()));
        def connection = url.openConnection(proxy)
        def response = connection.content.text
        println "Starting Active Scan: " + response
        if (response.contains("url_not_found")) // ZAP doesn't do status codes other than 200.
        {
            throw new RuntimeException("ZAP has no known links at " + project.zapConfig.applicationUrl)
        }

        checkStatusUntilScanComplete()
    }

    def checkStatusUntilScanComplete() {
        def responseText = "no responses yet"
        def responseCode = 200
        def maxRetries = 6 * project.zapConfig.activeScanTimeout.toInteger() // 10 second wait times 6 for one minute times number of minutes.
        def retryNum = 0
        while (!responseText.contains("100") && responseCode == 200)
        {
            if (retryNum >= maxRetries)
            {
                throw new RuntimeException("ZAP Active Scanner has not completed after ${project.zapConfig.activeScanTimeout} minutes. Exiting.")
            }
            def url = new URL("http://zap/JSON/ascan/view/status/?zapapiformat=JSON")

            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", project.zapConfig.proxyPort.toInteger()));
            def connection = url.openConnection(proxy)
            responseText = connection.content.text
            responseCode = connection.responseCode
            retryNum += 1
            sleep 10000
        }
    }
}