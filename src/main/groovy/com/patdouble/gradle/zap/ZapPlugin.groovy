/**
 * Copyright (c) 2018, Patrick Double. All right reserved.
 *
 * Released under BSD-3 style license.
 * See http://opensource.org/licenses/BSD-3-Clause
 */
package com.patdouble.gradle.zap

import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.Project
import org.gradle.api.Plugin

class ZapPlugin implements Plugin<Project> {
    final static String GROUP = 'verification'

    void apply(Project target) {
        target.extensions.create('zapConfig', ZapPluginExtension)

        CharSequence zapDir = "${target.gradle.gradleUserHomeDir}/zap/${target.extensions.zapConfig.version}"
        CharSequence zapInstallDir = "${zapDir}/ZAP_${target.extensions.zapConfig.version}"
        target.tasks.create('zapDownload', Download) {
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

        target.tasks.create('zapStart', ZapStart) {
            finalizedBy 'zapStop'
            dependsOn 'zapDownload'
            doFirst {
                if (!target.extensions.zapConfig.zapInstallDir) {
                    target.extensions.zapConfig.zapInstallDir = "${zapDir}/ZAP_${target.extensions.zapConfig.version}"
                }
            }
        }

        target.tasks.create('zapStop', ZapStop) {
            mustRunAfter target.tasks.zapStart
            mustRunAfter 'zapActiveScan'
            mustRunAfter 'zapReport'
        }

        target.tasks.create('zapSpider', ZapSpider) {
            dependsOn target.tasks.zapStart
            finalizedBy target.tasks.zapStop
        }

        target.tasks.create('zapAjaxSpider', ZapAjaxSpider) {
            dependsOn target.tasks.zapStart
            finalizedBy target.tasks.zapStop
        }

        target.tasks.create('zapActiveScan', ZapActiveScan) {
            dependsOn target.tasks.zapStart
            finalizedBy target.tasks.zapStop
            mustRunAfter target.tasks.zapSpider, target.tasks.zapAjaxSpider
        }

        target.tasks.create('zapReport', ZapReport) {
            dependsOn target.tasks.zapStart
            finalizedBy target.tasks.zapStop
            mustRunAfter target.tasks.zapSpider, target.tasks.zapAjaxSpider, target.tasks.zapActiveScan
        }

        target.tasks.create('zapInfo', ZapInfo) {
            dependsOn target.tasks.zapStart
            finalizedBy target.tasks.zapStop
        }
    }
}
