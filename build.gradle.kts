plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    kotlin("jvm") version "2.2.20"
}

group = "dev.mdma.qprotect.obfuscation"
version = "2.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("me.lucko:jar-relocator:1.7")
}

kotlin {
    jvmToolchain(8)
}

gradlePlugin {
    plugins {
        create("obfuscation-gradle-plugin") {
            id = "dev.mdma.qprotect.obfuscation"
            displayName = "qProtect Plugin"
            implementationClass = "dev.mdma.qprotect.obfuscation.QProtect"
            description = "A Gradle plugin for qProtect 2.0 obfuscation."
            tags.set(listOf())
        }
    }
}

publishing {
    repositories {
        maven {
            name = "qTechnologiesRepo"
            url = uri("https://nexus.mdma.dev/repository/maven-releases")
            credentials {
                username = findProperty("repo.user") as String? ?: System.getenv("REPO_USER")
                password = findProperty("repo.password") as String? ?: System.getenv("REPO_PASSWORD")
            }
        }
    }
}
