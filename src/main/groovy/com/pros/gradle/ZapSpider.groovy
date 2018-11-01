package com.pros.gradle

import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.progress.ProgressLogger
import org.gradle.internal.logging.progress.ProgressLoggerFactory
import org.zaproxy.clientapi.core.ClientApi

/**
 * Executes a ZAP spider against the applicationUrl. This task will wait until the scan is complete before returning.
 */
@Slf4j
class ZapSpider extends DefaultTask {
    @TaskAction
    @SuppressWarnings("UnusedMethod")
    void activeScan() {
        ClientApi zap = project.zapConfig.api()

        ProgressLogger progress = ((ProjectInternal) project).getServices().get(ProgressLoggerFactory).newOperation(ZapSpider)
        progress.start("Spidering ${project.zapConfig.applicationUrl}", 'initializing')

        String scanId = zap.spider.scan(project.zapConfig.applicationUrl, null, "true", null, "true").value
        logger.debug "Spider scan id = ${scanId}, all spiders = ${zap.spider.scans().toString(0)}"
        if (scanId) {
            int status
            while ((status = (zap.spider.status(scanId).value as int)) < 100) {
                progress.progress("${status}%")
                Thread.sleep(1000)
            }
            progress.completed('100%', false)
            logger.info "Spider results ${zap.spider.results(scanId).toString(0)}"
        } else {
            progress.completed('failed', true)
        }
    }
}
