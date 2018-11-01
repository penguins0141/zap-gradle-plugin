/**
 * Copyright (c) 2013, PROS Inc. All right reserved.
 * Copyright (c) 2018, Patrick Double. All right reserved.
 *
 * Released under BSD-3 style license.
 * See http://opensource.org/licenses/BSD-3-Clause
 */
package com.pros.gradle

import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.Project
import org.gradle.api.Plugin

class ZapPlugin implements Plugin<Project> {
    final String GROUP = 'verification'

    void apply(Project target) {
        target.extensions.create("zapConfig", ZapPluginExtension)

        def zapDir = "${target.gradle.gradleUserHomeDir}/zap/${target.extensions.zapConfig.version}"
        def zapInstallDir = "${zapDir}/ZAP_${target.extensions.zapConfig.version}"
        target.getTasks().create('zapDownload', Download) {
            CharSequence downloadUrl = "https://github.com/zaproxy/zaproxy/releases/download/${target.extensions.zapConfig.version}/ZAP_${target.extensions.zapConfig.version}_Crossplatform.zip"

            outputs.dir zapDir
            onlyIf { !target.extensions.zapConfig.zapInstallDir && !new File(zapInstallDir).exists() }

            group = GROUP
            description = 'Download ZAP'
            src downloadUrl
            dest new File(target.gradle.gradleUserHomeDir, "zap/${downloadUrl.split('/').last()}")
            overwrite false
            tempAndMove true

            doLast {
                target.copy {
                    from target.zipTree(dest)
                    into zapDir
                }
            }
        }

        target.getTasks().create('zapStart', ZapStart) {
            group = GROUP
            description = 'Starts the ZAP daemon.'
            finalizedBy 'zapStop'
            dependsOn 'zapDownload'
            doFirst {
                if (!target.extensions.zapConfig.zapInstallDir) {
                    target.extensions.zapConfig.zapInstallDir = "${zapDir}/ZAP_${target.extensions.zapConfig.version}"
                }
            }
        }

        target.getTasks().create('zapStop', ZapStop) {
            group = GROUP
            description = 'Stops the ZAP server ONLY if it has been started during this gradle process. Otherwise does nothing'
            mustRunAfter target.tasks.zapStart
            mustRunAfter 'zapActiveScan'
            mustRunAfter 'zapReport'
        }

        target.getTasks().create('zapSpider', ZapSpider) {
            group = GROUP
            description = 'Runs the ZAP spider against zapConfig.applicationUrl.'
            dependsOn target.tasks.zapStart
            finalizedBy target.tasks.zapStop
        }

        target.getTasks().create('zapActiveScan', ZapActiveScan) {
            group = GROUP
            description = 'Runs the ZAP active scanner against zapConfig.applicationUrl. It is recommended that this be done after any automated tests have completed so that the proxy is aware of those URLs.'
            dependsOn target.tasks.zapStart
            finalizedBy target.tasks.zapStop
            mustRunAfter target.tasks.zapSpider
        }

        target.getTasks().create('zapReport', ZapReport) {
            group = GROUP
            description = 'Generates a report with the current ZAP alerts for applicationUrl at reportOutputPath with type remoteFormat (HTML, JSON, or XML)'
            dependsOn target.tasks.zapStart
            finalizedBy target.tasks.zapStop
            mustRunAfter target.tasks.zapSpider, target.tasks.zapActiveScan
        }
    }
}





