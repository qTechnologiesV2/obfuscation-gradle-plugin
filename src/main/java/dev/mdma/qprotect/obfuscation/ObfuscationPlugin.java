package dev.mdma.qprotect.obfuscation;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ObfuscationPlugin implements Plugin<Project> {
    @Override
    public void apply(Project target) {
        target.getTasks().register("obfuscate", ObfuscateTask.class);
    }
}
