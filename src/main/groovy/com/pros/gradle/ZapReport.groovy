package com.pros.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.StopActionException
import org.gradle.api.tasks.TaskAction
import org.zaproxy.clientapi.core.ClientApi
import org.zaproxy.clientapi.core.ClientApiException

/**
 * Grabs the alert report from the running ZAP instances.
 */
class ZapReport extends DefaultTask {
    @TaskAction
    @SuppressWarnings("UnusedMethod")
    void outputReport() {
        ClientApi zap = project.zapConfig.api()
        try {
            zap.checkAlerts([], [], new File(project.buildDir, project.zapConfig.reportOutputPath as String))
        } catch (ClientApiException e) {
            throw new StopActionException(e.message)
        }
    }
}
