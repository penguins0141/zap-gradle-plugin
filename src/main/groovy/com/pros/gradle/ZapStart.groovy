package com.pros.gradle

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Starts the ZAP daemon. This will persist after the gradle run if stopZap is not called.
 */
class ZapStart extends DefaultTask {
    @TaskAction
    def startZap() {
        if (project.zapConfig.zapProc != null)
        {
            return
        }

        String workingDir = project.zapConfig.zapInstallDir
        def standardOutput = new ByteArrayOutputStream()
        def errorOutput = new ByteArrayOutputStream()
        Thread.start {
            ProcessBuilder builder
            if (Os.isFamily(Os.FAMILY_WINDOWS))
            {
                builder = new ProcessBuilder("java","-jar", "zap.jar", "-daemon", "-port", "${project.zapConfig.proxyPort.toInteger()}")
            }
            else
            {
                builder = new ProcessBuilder("/bin/bash", "-c", "java -jar zap.jar -daemon -port ${project.zapConfig.proxyPort.toInteger()}")
            }

            builder.directory(new File(workingDir))
            project.zapConfig.zapProc = builder.start()
            project.zapConfig.zapProc.consumeProcessOutput(standardOutput, errorOutput)
        }
        sleep 5000 // Wait for ZAP to start.
    }
}