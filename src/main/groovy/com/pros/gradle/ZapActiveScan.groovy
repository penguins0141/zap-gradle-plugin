package com.pros.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Executes a ZAP active scan against the applicationUrl. This task will wait until the scan is complete before returning.
 */
class ZapActiveScan extends DefaultTask {
    @TaskAction
    @SuppressWarnings("UnusedMethod")
    void activeScan() {
        String format = project.zapConfig.reportFormat
        URL url = new URL("http://zap/${format}/ascan/action/scan/?zapapiformat=${format}&url=${project.zapConfig.applicationUrl}&recurse=true&inScopeOnly=")

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress('localhost', project.zapConfig.proxyPort.toInteger()))
        def connection = url.openConnection(proxy)
        String response = connection.content.text
        println "Starting Active Scan: ${response}"
        if (response.contains("url_not_found")) // ZAP doesn't do status codes other than 200.
        {
            throw new RuntimeException("ZAP has no known links at " + project.zapConfig.applicationUrl)
        }

        checkStatusUntilScanComplete()
    }

    void checkStatusUntilScanComplete() {
        String responseText = "no responses yet"
        int responseCode = 200
        int maxRetries = 6 * project.zapConfig.activeScanTimeout.toInteger() // 10 second wait times 6 for one minute times number of minutes.
        int retryNum = 0
        while (!responseText.contains("100") && responseCode == 200)
        {
            if (retryNum >= maxRetries)
            {
                throw new RuntimeException("ZAP Active Scanner has not completed after ${project.zapConfig.activeScanTimeout} minutes. Exiting.")
            }
            URL url = new URL("http://zap/JSON/ascan/view/status/?zapapiformat=JSON")

            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress('localhost', project.zapConfig.proxyPort.toInteger()))
            def connection = url.openConnection(proxy)
            responseText = connection.content.text
            responseCode = connection.responseCode
            retryNum += 1
            sleep 10000
        }
    }
}