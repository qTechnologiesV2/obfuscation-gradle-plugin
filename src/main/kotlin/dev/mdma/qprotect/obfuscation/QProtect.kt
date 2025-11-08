package dev.mdma.qprotect.obfuscation

import dev.mdma.qprotect.obfuscation.tasks.QProtectCopyConfigTask
import dev.mdma.qprotect.obfuscation.tasks.QProtectObfuscateTask
import dev.mdma.qprotect.obfuscation.tasks.QProtectRelocateTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import java.io.File
import java.util.Properties
import java.util.jar.JarFile

class QProtect : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("qprotect", QProtectExtension::class.java)

        project.afterEvaluate {
            if (extension.qprotectJarPath.isPresent) {
                val qprotectJar = project.file(extension.qprotectJarPath.get())
                if (qprotectJar.exists()) {
                    try {
                        val version = readVersionFromJar(qprotectJar)
                        if (version != null) {
                            project.logger.info("Adding qProtect annotations dependency... version: $version")
                            project.repositories.maven {
                                url = project.uri("https://nexus.mdma.dev/repository/maven-releases/")
                            }
                            project.dependencies.add("compileOnly", "dev.mdma.qprotect:qprotect-annotations:$version")
                        }
                    } catch (e: Exception) {
                        project.logger.warn("Could not read version from qProtect jar: ${e.message}")
                    }
                }
            }
        }

        val tempDir = project.layout.buildDirectory.dir("qprotectTemp")
        val copyLibrariesTask = project.tasks.register("copyLibraries", QProtectRelocateTask::class.java) {
            relocations.set(extension.relocations)
            configurations.set(extension.configurations)
            outputDirectory.set(tempDir)
        }

        val copyConfigTask = project.tasks.register("qprotectCopyConfig", QProtectCopyConfigTask::class.java) {
            configFile.set(project.layout.file(extension.configPath.map { project.file(it) }))
            dependenciesPath.set(tempDir.map { it.asFile.absolutePath.replace("\\", "/") + "/" })
            outputConfigFile.set(extension.configPath.flatMap { configPath ->
                val fileName = project.file(configPath).name
                tempDir.map { it.file(fileName) }
            })
        }

        project.tasks.register("obfuscate", QProtectObfuscateTask::class.java) {
            dependsOn(copyLibrariesTask, copyConfigTask)

            qprotectJar.set(project.layout.file(extension.qprotectJarPath.map { project.file(it) }))
            inputJar.set(project.layout.file(extension.jarPath.map { project.file(it) }))
            configFile.set(copyConfigTask.flatMap { it.outputConfigFile })
            outputJar.set(project.layout.file(extension.outputJarPath.map { project.file(it) }))
            jvmArgs.set(extension.jvmArgs)
        }

        val cleanqProtectTask = project.tasks.register("qprotectClean", Delete::class.java) {
            group = "qprotect"
            description = "Cleans qProtect temporary files"
            delete(tempDir)
        }

        project.tasks.named("clean") {
            dependsOn(cleanqProtectTask)
        }
    }

    private fun readVersionFromJar(jarFile: File): String? {
        return try {
            JarFile(jarFile).use { jar ->
                val entry = jar.getEntry("qprotect.properties")
                if (entry != null) {
                    jar.getInputStream(entry).use { inputStream ->
                        val properties = Properties()
                        properties.load(inputStream)
                        properties.getProperty("version")
                    }
                } else {
                    null
                }
            }
        } catch (_: Exception) {
            null
        }
    }

}