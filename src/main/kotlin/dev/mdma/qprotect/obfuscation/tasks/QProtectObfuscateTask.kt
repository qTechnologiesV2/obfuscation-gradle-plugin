package dev.mdma.qprotect.obfuscation.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class QProtectObfuscateTask @Inject constructor(
    private val execOperations: ExecOperations
) : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val qprotectJar: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputJar: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val configFile: RegularFileProperty

    @get:OutputFile
    abstract val outputJar: RegularFileProperty

    @get:Input
    @get:Optional
    abstract val jvmArgs: ListProperty<String>

    init {
        group = "qprotect"
        description = "Runs qProtect obfuscation on the built jar"
    }

    @TaskAction
    fun obfuscate() {
        val qprotect = qprotectJar.get().asFile
        val input = inputJar.get().asFile
        val output = outputJar.get().asFile
        val config = configFile.get().asFile

        if (!qprotect.exists()) {
            throw GradleException("QProtect jar not found at: ${qprotect.absolutePath}")
        }
        if (!input.exists()) {
            throw GradleException("Input jar not found: ${input.absolutePath}")
        }
        if (!config.exists()) {
            throw GradleException("Config file not found at: ${config.absolutePath}")
        }

        output.parentFile.mkdirs()

        // Build command line arguments
        val commandLineArgs = mutableListOf("java")

        // Add JVM arguments if present
        val extraJvmArgs = jvmArgs.get()
        if (extraJvmArgs.isNotEmpty()) {
            commandLineArgs.addAll(extraJvmArgs)
            logger.info("Using JVM args: ${extraJvmArgs.joinToString(" ")}")
        }

        // Add jar execution arguments
        commandLineArgs.addAll(
            listOf(
                "-jar", qprotect.absolutePath,
                "-c", config.absolutePath,
                "-i", input.absolutePath,
                "-o", output.absolutePath
            )
        )

        logger.info("Executing: ${commandLineArgs.joinToString(" ")}")

        val result = execOperations.exec {
            commandLine(commandLineArgs)
            isIgnoreExitValue = false
        }

        if (result.exitValue != 0) {
            throw GradleException("qProtect obfuscation failed with exit code: ${result.exitValue}")
        }

        logger.lifecycle("Obfuscation complete: ${output.absolutePath}")
    }
}
