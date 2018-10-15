/**
 * Copyright (c) 2013, PROS Inc. All right reserved.
 * Copyright (c) 2018, Patrick Double. All right reserved.
 *
 * Released under BSD-3 style license.
 * See http://opensource.org/licenses/BSD-3-Clause
 */
package com.pros.gradle

import org.gradle.api.Project
import org.gradle.api.Plugin

class ZapPlugin implements Plugin<Project> {

    void apply(Project target) {
        target.extensions.create("zapConfig", ZapPluginExtension)

        target.getTasks().create('zapStart', ZapStart.class) {
            description = 'Starts the ZAP daemon. You must set the extension properties zap.jarPath and zap.proxyPort to the ZAP jar file location and the ZAP proxy port.'
            finalizedBy 'zapStop'
        }

        target.getTasks().create('zapStop', ZapStop) {
            description = 'Stops the ZAP server ONLY if it has been started during this gradle process. Otherwise does nothing'
            mustRunAfter project.tasks.zapStart
            mustRunAfter 'zapActiveScan'
            mustRunAfter 'zapReport'
        }

        target.getTasks().create('zapActiveScan', ZapActiveScan.class) {
            description = 'Runs the ZAP active scanner against zap.applicationUrl. It is recommended that this be done after any automated tests have completed so that the proxy is aware of those URLs.'
            dependsOn project.tasks.zapStart
            finalizedBy project.tasks.zapStop
        }

        target.getTasks().create('zapReport', ZapReport.class) {
            description = 'Generates a report with the current ZAP alerts for applicationUrl at reportOutputPath with type remoteFormat (HTML, JSON, or XML)'
            dependsOn project.tasks.zapStart
            finalizedBy project.tasks.zapStop
        }
    }
}





