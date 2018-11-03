package com.pros.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.zaproxy.clientapi.core.ClientApi

/**
 * Grabs the alert report from the running ZAP instances.
 */
class ZapReport extends DefaultTask {
    @TaskAction
    @SuppressWarnings("UnusedMethod")
    void outputReport() {
        ClientApi zap = project.zapConfig.api()
        if (!project.zapConfig.reportFormat) {
            project.zapConfig.reportFormat = 'json,xml,html,md'
        }
        new File(project.buildDir, project.zapConfig.reportOutputPath).parentFile.mkdirs()
        project.zapConfig.reportFormat.toLowerCase().split(/[^a-z]+/).asType(List).unique().each { format ->
            switch (format) {
                case 'json':
                    new File(project.buildDir, "${project.zapConfig.reportOutputPath}.json").bytes = zap.core.jsonreport()
                    break
                case 'xml':
                    new File(project.buildDir, "${project.zapConfig.reportOutputPath}.xml").bytes = zap.core.xmlreport()
                    break
                case 'html':
                    new File(project.buildDir, "${project.zapConfig.reportOutputPath}.html").bytes = zap.core.htmlreport()
                    break
                case 'md':
                    new File(project.buildDir, "${project.zapConfig.reportOutputPath}.md").bytes = zap.core.mdreport()
                    break
                default:
                    throw new GradleException("Unknown report format: ${format}")
            }
        }
    }
}
