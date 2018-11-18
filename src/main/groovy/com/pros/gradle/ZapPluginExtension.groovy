package com.pros.gradle

import org.zaproxy.clientapi.core.ClientApi

class ZapPluginExtension {
    String version = '2.7.0'
    String zapInstallDir = ''
    String proxyPort = ''
    /** Comma and/or space separated list: json, html, xml, md or leave empty for all.*/
    String reportFormat = ''
    String reportOutputPath = 'reports/zap/zapReport'
    String applicationUrl = ''
    String activeScanTimeout = '30'
    String apiKey = UUID.randomUUID()
    List<String> parameters = []
    protected Process zapProc

    ClientApi api() {
        new ClientApi('localhost', proxyPort as int, apiKey)
    }

    void api(Closure closure) {
        closure.delegate = api()
        closure.call()
    }
}
