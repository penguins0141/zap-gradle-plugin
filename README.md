# ZAP Gradle Plugin

Automate web application dynamic security scans using OWASP ZAP.

Originally based off plugin found here: https://github.com/PROSPricing/zap-gradle-plugin

## TL;DR

```groovy
plugins { 
    id 'com.patdouble.gradle.zap'
}

zapConfig {
    applicationUrl = "http://attackme.example.com:8080"
}
```

```bash
$ ./gradlew zapSpider zapActiveScan zapReport
$ ls -1 build/reports/zap
zapReport.err.log
zapReport.html
zapReport.json
zapReport.md
zapReport.out.log
zapReport.xml
```

## Getting the Plugin

The plugin is available from the Gradle plugins repository using the usual methods.

```groovy
plugins {
    id 'com.patdouble.zap'
}
```

or

```groovy
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "gradle.plugin.com.patdouble:zap-gradle-plugin:2.+"
  }
}

apply plugin: "com.patdouble.zap"
```

## Finding the ZAP Application

By default the plugin will download version 2.7.0 of OWASP ZAP and use it. You can specify a version by including the following
in your `build.gradle`:

```groovy
zapConfig {
    version = "2.6.0"
}
```

If you'd rather use an already installed version, configure the install directory. The directory must include either
`zap.sh` or `zap.bat`, the script used to start ZAP.

```groovy
zapConfig {
    zapInstallDir =  "/path/to/ZAP/install/directory"
}
```

## Configure Your Application

At a minimum you must configure the URL of your application that ZAP is to scan.

```groovy
zapConfig {
    applicationUrl = "http://attackme.example.com:8080"
}
```

## Optional Properties
There are a few optional properties that may be specified within the zapConfig section of the gradle file to further tune your use of ZAP.

```groovy
zapConfig {
    // The port on which ZAP should run. Defaults to a free port.
    proxyPort = "9999"
    // The format of the output report. Acceptable formats are JSON, HTML, MD and XML. Defaults to all.
    reportFormat = "JSON"
    // The path of the report file to write from the zapReport task. This path must be writable, subdirs will be created.
    reportOutputPath = "report"
    // The timeout for the active scanner process. Defaults to 30 seconds.
    activeScanTimeout = "30"
}
```

## Running ZAP with Tests
`./gradlew zapStart taskThatRunsMyTestsWithALocalProxySetToZAPProxyPort zapActiveScan zapReport zapStop`

## Updating Tests to Use ZAP

In order for ZAP to see traffic to your app, it must be used as a proxy for those requests. Different testing tools will have different mechanisms for setting up a proxy for a given HTTP request. Some examples are below.

Python with httplib2:
```python
http_con = httplib2.Http(proxy_info = httplib2.ProxyInfo(httplib2.socks.PROXY_TYPE_HTTP, 'localhost', proxyPort))
```

Java/Groovy with URLConnection:
```java
URL url = new URL("http://attackme.example.com");
Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", proxyPort));
URLConnection connection = url.openConnection(proxy);
```

Ruby:
```ruby
proxy_addr = 'localhost'
proxy_port = 9999

Net::HTTP.new('attackme.example.com', nil, proxy_addr, proxy_port).start { |http|
  # always proxy via your.proxy.addr:9999
}
```

## LICENSE
Copyright (c) 2018, Patrick Double. All right reserved.

Released under BSD-3 style license.

See http://opensource.org/licenses/BSD-3-Clause and LICENSE file for details.
