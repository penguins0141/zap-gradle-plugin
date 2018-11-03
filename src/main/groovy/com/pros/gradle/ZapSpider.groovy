package com.pros.gradle

import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.progress.ProgressLogger
import org.zaproxy.clientapi.core.ClientApi

/**
 * Executes a ZAP spider against the applicationUrl. This task will wait until the scan is complete before returning.
 */
@Slf4j
class ZapSpider extends DefaultTask implements ZapTaskHelper {
    @TaskAction
    @SuppressWarnings("UnusedMethod")
    void activeScan() {
        ClientApi zap = project.zapConfig.api()
        ProgressLogger progress = createProgressLogger()
        progress.start("Spidering ${project.zapConfig.applicationUrl}", 'initializing')

        String scanId = zap.spider.scan(project.zapConfig.applicationUrl, null, "true", null, "true").value
        logger.debug "Spider scan id = ${scanId}"
        if (scanId) {
            waitForCompletion(zap, progress, project.zapConfig.activeScanTimeout as int,
                    { zap.spider.status(scanId).value as int },
                    { zap.spider.results(scanId).items.size() as String }
            )

            logger.debug "Spider results ${zap.spider.results(scanId).toString(0)}"
        } else {
            progress.completed('failed', true)
        }
    }
}
