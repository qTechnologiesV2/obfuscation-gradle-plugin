# qProtect Gradle Plugin

A Gradle plugin for obfuscating compiled JAR files using qProtect and qProtect Lite.

## Prerequisites

- Java 8 or higher
- qProtect JAR file

## Installation

### 1. Configure Plugin Repository

Add the plugin repository to your `settings.gradle` (Groovy) or `settings.gradle.kts` (Kotlin):

**Kotlin DSL:**
```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://nexus.mdma.dev/repository/maven-releases")
    }
}
```

**Groovy DSL:**
```groovy
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url 'https://nexus.mdma.dev/repository/maven-releases' }
    }
}
```

### 2. Apply the Plugin

Add the plugin to your `build.gradle` or `build.gradle.kts`:

**Kotlin DSL:**
```kotlin
plugins {
    id("java")
    id("dev.mdma.qprotect.obfuscation") version "2.0"
}
```

**Groovy DSL:**
```groovy
plugins {
    id 'java'
    id 'dev.mdma.qprotect.obfuscation' version '2.0'
}
```

## Configuration

Configure the obfuscation task in your build file:

**Kotlin DSL:**
```kotlin
qprotect {
    // Required: Path to the JAR file to obfuscate
    jarPath.set("build/libs/myapp.jar")

    // Required: Path for the obfuscated output JAR
    outputJarPath.set("build/libs/myapp-obfuscated.jar")

    // Required: Path to the qProtect JAR file
    qprotectJarPath.set("tools/qprotect.jar")

    // Required: Path to the qProtect configuration file
    configPath.set("${project.rootDir}/obf-settings.toml")

    // Optional: Package relocations (useful for resolving conflicts)
    relocations.set(mapOf(
        "org.bson" to "com.myapp.shaded.bson",
        "com.google.common" to "com.myapp.shaded.guava"
    ))
}
```

**Groovy DSL:**
```groovy
qprotect {
    jarPath = 'build/libs/myapp.jar'
    outputJarPath = 'build/libs/myapp-obfuscated.jar'
    qprotectJarPath = 'tools/qprotect.jar'
    configPath = "${project.rootDir}/obf-settings.toml"

    relocations = [
            'org.bson': 'com.myapp.shaded.bson',
            'com.google.common': 'com.myapp.shaded.guava'
    ]
}
```

## Configuration File

```toml
# Important: Set the libraries path for dependency resolution
libraries = [
    "${dependenciesPath}"
]
```

## Usage

Run the obfuscation task:

```bash
./gradlew obfuscate
```

Or make it run automatically after building:

```kotlin
tasks.named("build") { // or shadowJar, etc.
    finalizedBy("obfuscate")
}
```

## Configuration Options

| Property          | Type                  | Required | Description                                      |
|-------------------|-----------------------|----------|--------------------------------------------------|
| `jarPath`         | `File`                | Yes      | Path to the input JAR file to obfuscate          |
| `outputJarPath`   | `File`                | Yes      | Path where the obfuscated JAR will be saved      |
| `qprotectJarPath` | `File`                | Yes      | Path to the qProtect executable JAR              |
| `configPath`      | `String`              | Yes      | Path to the qProtect TOML configuration file     |
| `relocations`     | `Map<String, String>` | No       | Package relocation mappings to resolve conflicts |

**Note:** The `${dependenciesPath}` variable is automatically provided by the plugin and points to your project's dependencies.
