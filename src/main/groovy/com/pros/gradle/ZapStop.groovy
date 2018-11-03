package com.pros.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class ZapStop extends DefaultTask {
    @TaskAction
    @SuppressWarnings("UnusedMethod")
    void stopZap() {
        if (project.zapConfig.zapProc != null)
        {
            project.zapConfig.api().core.shutdown()
            // Kill the process after waiting. The Process API doesn't expose kill directly.
            project.zapConfig.zapProc.waitForOrKill(5000)
        }
    }
}