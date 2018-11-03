package com.pros.gradle

import groovy.util.logging.Slf4j
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.zaproxy.clientapi.core.ClientApi

/**
 * Starts the ZAP daemon. This will persist after the gradle run if stopZap is not called.
 */
@Slf4j
class ZapStart extends DefaultTask {
    @TaskAction
    @SuppressWarnings("UnusedMethod")
    void startZap() {
        if (project.zapConfig.zapProc != null)
        {
            return
        }

        String workingDir = project.zapConfig.zapInstallDir
        project.zapConfig.proxyPort = resolvePort()
        def standardOutput = new ByteArrayOutputStream()
        def errorOutput = new ByteArrayOutputStream()
        ProcessBuilder builder = new ProcessBuilder(
            workingDir+File.separator+(Os.isFamily(Os.FAMILY_WINDOWS) ? "zap.bat":"zap.sh"),
            "-daemon", "-port", project.zapConfig.proxyPort, "-config", "api.key=${project.zapConfig.apiKey}")

        builder.directory(new File(workingDir))
        logger.info "Running ZAP using ${builder.command()} in ${builder.directory()}"
        Thread.start {
            project.zapConfig.zapProc = builder.start()
            project.zapConfig.zapProc.consumeProcessOutput(standardOutput, errorOutput)
        }

        ClientApi zap = new ClientApi('localhost', project.zapConfig.proxyPort as int)
        zap.waitForSuccessfulConnectionToZap(120)
    }

    protected String resolvePort() {
        if (project.zapConfig.proxyPort) {
            return project.zapConfig.proxyPort
        }

        Integer port = null
        ServerSocket socket = null
        try {
            socket = new ServerSocket(0)
            port = socket.getLocalPort()
        } finally {
            socket?.close()
        }
        return port as String
    }
}
