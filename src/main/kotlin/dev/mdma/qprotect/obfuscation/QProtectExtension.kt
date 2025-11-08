package dev.mdma.qprotect.obfuscation

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import javax.inject.Inject

abstract class QProtectExtension @Inject constructor(objects: ObjectFactory) {

    val jarPath: Property<String> = objects.property(String::class.java)

    val outputJarPath: Property<String> = objects.property(String::class.java)

    val configPath: Property<String> = objects.property(String::class.java)

    val qprotectJarPath: Property<String> = objects.property(String::class.java)
        .convention(System.getProperty("user.home") + "/qprotect.jar")

    val annotationsVersionFallback: Property<String> = objects.property(String::class.java)
        .convention("2.0.0-beta7")

    val relocations: MapProperty<String, String> =
        objects.mapProperty(String::class.java, String::class.java)
            .convention(emptyMap())

    val jvmArgs: ListProperty<String> = objects.listProperty(String::class.java)
        .convention(emptyList())

    val configurations: SetProperty<String> = objects.setProperty(String::class.java)
        .convention(setOf("compileClasspath", "runtimeClasspath"))

}
