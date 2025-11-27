package dev.mdma.qprotect.obfuscation.tasks

import me.lucko.jarrelocator.JarRelocator
import me.lucko.jarrelocator.Relocation
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class QProtectRelocateTask : DefaultTask() {

    @get:Input
    abstract val relocations: MapProperty<String, String>

    @get:Input
    abstract val configurations: SetProperty<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    init {
        group = "qprotect"
        description = "Relocates dependency packages for obfuscation"
    }

    @TaskAction
    fun relocateArtifacts() {
        val outputDir = outputDirectory.get().asFile
        outputDir.mkdirs()

        val relocationList = relocations.get().map { (key, value) ->
            Relocation(key, value)
        }

        val processedFiles = mutableSetOf<File>()
        val configNames = configurations.get()

        configNames.forEach { configName ->
            project.configurations.findByName(configName)?.let { config ->
                try {
                    config.resolvedConfiguration
                        .resolvedArtifacts
                        .map { it.file }
                        .forEach { file ->
                            if (file !in processedFiles && file.isFile && file.name.endsWith(".jar")) {
                                processedFiles.add(file)
                                if (relocationList.isEmpty()) {
                                    val targetFile = File(outputDir, file.name)
                                    file.copyTo(targetFile, overwrite = true)
                                    logger.lifecycle("Copied ${file.name} to ${targetFile.absolutePath} without relocations")
                                } else {
                                    try {
                                        logger.info("Relocating: ${file.name}")
                                        val relocator = JarRelocator(
                                            file,
                                            outputDir.resolve(file.name),
                                            relocationList
                                        )
                                        relocator.run()
                                    } catch (e: Exception) {
                                        throw GradleException("Failed to relocate ${file.name}: ${e.message}", e)
                                    }
                                }
                            }
                        }
                } catch (e: Exception) {
                    logger.warn("Could not resolve configuration $configName: ${e.message}")
                }
            }
        }

        logger.lifecycle("Relocated ${processedFiles.size} artifacts")
    }
}