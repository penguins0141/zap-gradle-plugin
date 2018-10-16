package com.pros.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Grabs the alert report from the running ZAP instances.
 */
class ZapReport extends DefaultTask {
    @TaskAction
    @SuppressWarnings("UnusedMethod")
    void outputReport() {
        String format = project.zapConfig.reportFormat
        URL url = new URL("http://zap/${format}/core/view/alerts/?zapapiformat=${format}&baseurl=${project.zapConfig.applicationUrl}")

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress('localhost', project.zapConfig.proxyPort.toInteger()))
        def connection = url.openConnection(proxy)
        String response = connection.content.text

        File report = new File(project.zapConfig.reportOutputPath as String)
        report.write(response)
    }
}