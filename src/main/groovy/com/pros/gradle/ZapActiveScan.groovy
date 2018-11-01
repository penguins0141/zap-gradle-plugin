package com.pros.gradle

import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.progress.ProgressLogger
import org.gradle.internal.logging.progress.ProgressLoggerFactory
import org.zaproxy.clientapi.core.ClientApi

/**
 * Executes a ZAP active scan against the applicationUrl. This task will wait until the scan is complete before returning.
 */
@Slf4j
class ZapActiveScan extends DefaultTask {
    @TaskAction
    @SuppressWarnings("UnusedMethod")
    void activeScan() {
        ClientApi zap = project.zapConfig.api()

        ProgressLogger progress = ((ProjectInternal) project).getServices().get(ProgressLoggerFactory).newOperation(ZapSpider)
        progress.start("Active scan ${project.zapConfig.applicationUrl}", 'initializing')

        String scanId = zap.ascan.scan(project.zapConfig.applicationUrl, "true", "true", "", "", "").value
        logger.info "Active scan id = ${scanId}, all scans = ${zap.ascan.scans().toString(0)}"
        if (scanId) {
            int status
            while ((status = (zap.ascan.status(scanId).value as int)) < 100) {
                progress.progress("${status}%")
                Thread.sleep(1000)
            }
            logger.info "Active scan id = ${scanId}, all scans = ${zap.ascan.scans().toString(0)}"
            progress.completed('100%', false)
        } else {
            progress.completed('failed', true)
        }
    }
}
