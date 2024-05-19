
# obfuscation-gradle-plugin
A gradle plugin used to obfuscate a compiled file with qProtect and qProtect Lite

## Instructions
- add our repository as pluginRepository to your settings.gradle

```kotlin
pluginManagement {
    repositories {
        maven("https://nexus.mdma.dev/repository/maven-releases")
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "dev.mdma.qprotect.obfuscation") {
                useModule("dev.mdma.qprotect:obfuscation:1.0")
            }
        }
    }
}
```

 - add our plugin into your build.gradle

```kotlin
plugins {
    id("java")
    id("dev.mdma.qprotect.obfuscation") version "1.0"
}
```
 - add the task to your build.gradle and set the paths
```kotlin
tasks {
    obfuscate {
        obfuscatorPath = File("qprotect-core-1.11.0-release.jar")
        configPath = File("config.yml")
        //inputFile and outputFile is optional it can be set here or in the config
        inputFile = File("input.jar")
        outputFile = File("output.jar")
        //javaPath is only required if your project or gradle doesn't use java 8
        javaPath = File("C:/Program Files/Amazon Corretto/jdk1.8.0_392/jre")
    }
}
```
- then run the task obfuscate


