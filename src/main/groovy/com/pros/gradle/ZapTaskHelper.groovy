package com.pros.gradle

import org.gradle.api.GradleException
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.internal.logging.progress.ProgressLogger
import org.gradle.internal.logging.progress.ProgressLoggerFactory
import org.zaproxy.clientapi.core.ClientApi

trait ZapTaskHelper {
    ProgressLogger createProgressLogger() {
        return ((ProjectInternal) project).getServices().get(ProgressLoggerFactory).newOperation(this.class)
    }

    void waitForCompletion(ClientApi zap, ProgressLogger progress, int timeout, Closure<Integer> fetchStatus, Closure<String> progressMessage = null) {
        final long end = (timeout > 0) ? System.currentTimeMillis() + timeout*1000 : Long.MAX_VALUE
        int status
        while ((status = fetchStatus.call()) < 100) {
            if (System.currentTimeMillis() > end) {
                String msg = "operation did not complete within ${timeout} seconds"
                progress?.completed(msg, true)
                throw new GradleException(msg)
            }
            String msg = (progressMessage?.call()) ?: ''
            progress?.progress("${status}%, ${msg}")
            Thread.sleep(2000)
        }
        progress?.completed('100%', false)
    }
}
