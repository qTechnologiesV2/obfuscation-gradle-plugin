package dev.mdma.qprotect.obfuscation.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

abstract class QProtectCopyConfigTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val configFile: RegularFileProperty

    @get:Input
    abstract val dependenciesPath: Property<String>

    @get:OutputFile
    abstract val outputConfigFile: RegularFileProperty

    init {
        group = "qprotect"
        description = "Copies and expands obfuscation configuration"
    }

    @TaskAction
    fun copyConfig() {
        val input = configFile.get().asFile
        val output = outputConfigFile.get().asFile
        val depPath = dependenciesPath.get()

        output.parentFile.mkdirs()

        val content = input.readText()
        val expanded = content.replace("\${dependenciesPath}", depPath)
        output.writeText(expanded)

        logger.info("Expanded config written to: ${output.absolutePath}")
    }
}