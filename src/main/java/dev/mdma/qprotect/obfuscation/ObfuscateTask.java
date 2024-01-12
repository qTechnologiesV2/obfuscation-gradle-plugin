package dev.mdma.qprotect.obfuscation;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public abstract class ObfuscateTask extends DefaultTask {

    @Input
    abstract public Property<File> getObfuscatorPath();

    @Input
    abstract public Property<File> getConfigPath();

    @Input
    @Optional
    abstract public Property<File> getInputFile();

    @Input
    @Optional
    abstract public Property<File> getOutputFile();

    @Input
    @Optional
    abstract public Property<File> getJavaPath();

    @TaskAction
    public void run() {
        // check if the qProtect executable exists
        if (!this.getObfuscatorPath().get().exists())
            throw new TaskExecutionException(this, new IOException("qProtect Obfuscator Path is null or does not exist."));

        // check if the provided config exists
        if (!this.getConfigPath().get().exists())
            throw new TaskExecutionException(this, new IOException("qProtect Obfuscator Config Path is null or does not exist."));

        // If the default java path on your system isn't java8, get the java8 path from build.gradle
        if (this.getJavaPath().isPresent())
            this.getLogger().quiet("Using custom java path " + this.getJavaPath().get());

        try {
            // Start the qProtect process
            final Process process = this.createProcess();

            // Capture qProtects log
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    this.getLogger().quiet(line);
                }
            }
            // Capture qProtects exceptions
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    this.getLogger().error(errorLine);
                }
            }

            final int exitCode = process.waitFor();

            // If the exitCode isn't 0, something went wrong
            if (exitCode != 0)
                throw new TaskExecutionException(this, new RuntimeException("qProtect failed with exit code: " + exitCode));

        } catch (IOException | InterruptedException e) {
            throw new TaskExecutionException(this, e);
        }
    }

    private Process createProcess() throws IOException {
        final ProcessBuilder processBuilder = new ProcessBuilder(
                !this.getJavaPath().isPresent() ? "java" : this.getJavaPath().get() + File.separator + "bin" + File.separator + "java",
                "-jar",
                this.getObfuscatorPath().get().getAbsolutePath(),
                "--config",
                this.getConfigPath().get().getAbsolutePath()
        );

        if (this.getInputFile().isPresent()) {
            processBuilder.command().add("--input");
            processBuilder.command().add(this.getInputFile().get().getAbsolutePath());
        }
        if (this.getOutputFile().isPresent()) {
            processBuilder.command().add("--output");
            processBuilder.command().add(this.getOutputFile().get().getAbsolutePath());
        }

        processBuilder.redirectErrorStream(true);
        return processBuilder.start();
    }
}
