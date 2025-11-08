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
    id("dev.mdma.qprotect.obfuscation") version "2.0.1"
}
```

**Groovy DSL:**
```groovy
plugins {
    id 'java'
    id 'dev.mdma.qprotect.obfuscation' version '2.0.1'
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

    // Required: Path to the qProtect JAR file (defaults to ~/qprotect.jar)
    qprotectJarPath.set("tools/qprotect.jar")

    // Required: Path to the qProtect configuration file
    configPath.set("${project.rootDir}/obf-settings.toml")

    // Optional: Package relocations (useful for resolving conflicts)
    relocations.set(mapOf(
        "org.bson" to "com.myapp.shaded.bson",
        "com.google.common" to "com.myapp.shaded.guava"
    ))

    // Optional: JVM arguments for the obfuscation process
    jvmArgs.set(listOf(
        "-Xmx2G",
        "-Xms512M"
    ))

    // Optional: Gradle configurations to process for dependencies
    configurations.set(setOf("compileClasspath", "runtimeClasspath"))
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

    jvmArgs = ['-Xmx2G', '-Xms512M']

    configurations = ['compileClasspath', 'runtimeClasspath']
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

### Running Obfuscation

Run the obfuscation task:

```bash
./gradlew obfuscate
```

### Auto-run After Build

Make obfuscation run automatically after building:

**Kotlin DSL:**
```kotlin
tasks.named("build") {
    finalizedBy("obfuscate")
}
```

**Groovy DSL:**
```groovy
tasks.named('build') {
    finalizedBy 'obfuscate'
}
```

## Available Tasks

| Task                     | Description                                      |
|--------------------------|--------------------------------------------------|
| `obfuscate`              | Main task: runs full obfuscation process         |
| `qprotectCopyLibraries`  | Copies and relocates library dependencies        |
| `qprotectCopyConfig`     | Copies and expands obfuscation configuration     |
| `qprotectClean`          | Cleans qProtect temporary files                  |

## Configuration Options

| Property          | Type                  | Required | Default                              | Description                                           |
|-------------------|-----------------------|----------|--------------------------------------|-------------------------------------------------------|
| `jarPath`         | `String`              | Yes      | -                                    | Path to the input JAR file to obfuscate               |
| `outputJarPath`   | `String`              | Yes      | -                                    | Path where the obfuscated JAR will be saved           |
| `qprotectJarPath` | `String`              | No       | `~/qprotect.jar`                     | Path to the qProtect executable JAR                   |
| `configPath`      | `String`              | Yes      | -                                    | Path to the qProtect TOML configuration file          |
| `relocations`     | `Map<String, String>` | No       | `emptyMap()`                         | Package relocation mappings to resolve conflicts      |
| `jvmArgs`         | `List<String>`        | No       | `emptyList()`                        | JVM arguments for the obfuscation process             |
| `configurations`  | `Set<String>`         | No       | `compileClasspath, runtimeClasspath` | Gradle configurations to process for dependencies     |

### Build Lifecycle Integration

The plugin integrates with Gradle's build lifecycle:
- `obfuscate` automatically depends on `jar` task (if configured)
- `clean` automatically runs `qprotectClean`
- Incremental builds and caching supported