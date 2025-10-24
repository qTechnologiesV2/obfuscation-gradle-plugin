package dev.mdma.qprotect.obfuscation

import me.lucko.jarrelocator.JarRelocator
import me.lucko.jarrelocator.Relocation
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec

class QProtect : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("qprotect", QProtectExtension::class.java)
        val tempDir = project.layout.buildDirectory.dir("qprotectTemp")

        val copyArtifactsTask = project.tasks.register("copyArtifacts") {
            doFirst {
                project.delete(tempDir)
            }

            doLast {
                val outputDir = tempDir.get().asFile
                outputDir.mkdirs()

                val relocations = mutableListOf<Relocation>()

                relocations.addAll(
                    extension.relocations.get().map { (key, value) ->
                        Relocation(key, value)
                    }
                )

                project.configurations.getByName("compileClasspath")
                    .resolvedConfiguration
                    .resolvedArtifacts
                    .map { it.file }
                    .forEach { file ->
                        if (file.isFile && file.name.endsWith(".jar")) {
                            val relocator = JarRelocator(
                                file,
                                outputDir.resolve(file.name),
                                relocations
                            )
                            relocator.run()
                        } else {
                            project.logger.lifecycle("Skipping non-JAR file: ${file.path}")
                        }
                    }
            }
        }

        val copyConfigTask = project.tasks.register("copyObfuscationConfig", Copy::class.java) {
            from(extension.configPath.map { project.file(it) })
            into(tempDir)
            expand(mapOf("dependenciesPath" to tempDir.get().asFile.path.replace("\\", "/") + "/"))
        }

        val configFileProvider: Provider<RegularFile> = extension.configPath.flatMap { configPath ->
            val fileName = project.file(configPath).name
            tempDir.map { it.file(fileName) }
        }

        val deleteArtifactsTask = project.tasks.register("deleteArtifacts") {
            doLast {
                project.delete(tempDir)
            }
        }

        project.tasks.register("obfuscate", Exec::class.java) {
            group = "build"
            description = "Runs qProtect obfuscation on the built jar."

            dependsOn(copyArtifactsTask, copyConfigTask)

            inputs.file(extension.jarPath.map { project.file(it) })
            inputs.file(configFileProvider)
            outputs.file(extension.outputJarPath.map { project.file(it) })

            doFirst {
                val qprotectJar = project.file(extension.qprotectJarPath.get())
                val inputJar = project.file(extension.jarPath.get())
                val outputJar = project.file(extension.outputJarPath.get())
                val configFile = configFileProvider.get().asFile

                if (!qprotectJar.exists()) {
                    throw GradleException("QProtect jar not found at: ${qprotectJar.absolutePath}")
                }
                if (!inputJar.exists()) {
                    throw GradleException("Input jar not found: ${inputJar.absolutePath}")
                }
                if (!configFile.exists()) {
                    throw GradleException("Expanded config file not found at: ${configFile.absolutePath}")
                }

                val argsList = mutableListOf(
                    "java",
                    "-jar", qprotectJar.absolutePath,
                    "-c", configFile.absolutePath,
                    "-i", inputJar.absolutePath,
                    "-o", outputJar.absolutePath
                )

                commandLine(argsList)
            }

            finalizedBy(deleteArtifactsTask)
        }
    }

}