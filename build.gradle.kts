plugins {
    id("java")
    id("java-gradle-plugin")
    id("maven-publish")
}

group = "dev.mdma.qprotect"
version = "1.0"

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("qProtectObfuscation") {
            id = "dev.mdma.qprotect.obfuscation"
            implementationClass = "dev.mdma.qprotect.obfuscation.ObfuscationPlugin"
        }
    }
}

tasks.register("publishAll") {
    dependsOn("assemble")
    dependsOn("publish")
    dependsOn("publishPluginMavenPublicationToMavenLocal")
    dependsOn("publishQProtectObfuscationPluginMarkerMavenPublicationToMavenLocal")
    dependsOn("publishToMavenLocal")
}
