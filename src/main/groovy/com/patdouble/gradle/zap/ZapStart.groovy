package com.patdouble.gradle.zap

import groovy.util.logging.Slf4j
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.zaproxy.clientapi.core.ClientApi

/**
 * Starts the ZAP daemon. This will persist after the gradle run if stopZap is not called.
 */
@Slf4j
class ZapStart extends DefaultTask implements ZapTaskHelper {
    @SuppressWarnings('LineLength')
    ZapStart() {
        group = ZapPlugin.GROUP
        description = 'Starts the ZAP daemon.'
    }

    @TaskAction
    @SuppressWarnings('UnusedMethod')
    void startZap() {
        if (project.zapConfig.zapProc != null) {
            return
        }

        // Check for a running ZAP listening on the port, which is useful for debugging configuration
        if (isZapRunning(project.zapConfig)) {
            return
        }

        String workingDir = project.zapConfig.zapInstallDir
        project.zapConfig.proxyPort = resolvePort()

        List<String> command = [ workingDir + File.separator + (Os.isFamily(Os.FAMILY_WINDOWS) ? 'zap.bat' : 'zap.sh'),
                '-daemon', '-port', project.zapConfig.proxyPort, '-config', "api.key=${project.zapConfig.apiKey}" as String ]
        command.addAll(project.zapConfig.parameters.collect { it as String })
        ProcessBuilder builder = new ProcessBuilder(command)
        builder.redirectOutput(new File(project.buildDir, "${project.zapConfig.reportOutputPath}.out.log"))
        builder.redirectError(new File(project.buildDir, "${project.zapConfig.reportOutputPath}.err.log"))

        builder.directory(new File(workingDir))
        logger.info "Running ZAP using ${builder.command()} in ${builder.directory()}"
        Thread.start {
            project.zapConfig.zapProc = builder.start()
        }

        ClientApi zap = new ClientApi('localhost',
                project.zapConfig.proxyPort as int,
                project.zapConfig.apiKey as String)
        zap.waitForSuccessfulConnectionToZap(120)

        zap.core.setMode('protect')
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
