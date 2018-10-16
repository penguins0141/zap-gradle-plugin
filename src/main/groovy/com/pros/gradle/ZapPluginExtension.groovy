package com.pros.gradle

class ZapPluginExtension {
    String zapInstallDir = ""
    String proxyPort = "54300"
    String reportFormat = "JSON"
    String reportOutputPath = "zapReport"
    String applicationUrl = ""
    String activeScanTimeout = "30"
    protected Process zapProc
}
