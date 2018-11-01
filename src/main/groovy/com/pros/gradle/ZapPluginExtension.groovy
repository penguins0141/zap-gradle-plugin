package com.pros.gradle

import org.zaproxy.clientapi.core.ClientApi

class ZapPluginExtension {
    String version = '2.7.0'
    String zapInstallDir = ""
    String proxyPort = ""
    String reportOutputPath = "reports/zapReport.xml"
    String applicationUrl = ""
    String activeScanTimeout = "30"
    protected Process zapProc

    ClientApi api() {
        new ClientApi('localhost', proxyPort as int)
    }
}
