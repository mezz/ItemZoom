plugins {
    id("java")
    id("idea")
    id("eclipse")
    // https://projects.neoforged.net/neoforged/neogradle
    id("net.neoforged.gradle.userdev") version("7.0.154")
}

// gradle.properties
val neoForgeVersion: String by extra
val neoForgeVersionRange: String by extra
val githubUrl: String by extra
val minecraftVersion: String by extra
val minecraftVersionRange: String by extra
val modAuthor: String by extra
val modDescription: String by extra
val modGroup: String by extra
val modId: String by extra
val modName: String by extra
val modJavaVersion: String by extra
val specificationVersion: String by extra
val jeiVersion: String by extra
val jeiVersionRange: String by extra
val loaderVersionRange: String by extra
val parchmentMappingsMinecraftVersion: String by extra
val parchmentMappingsVersion: String by extra

// these are required for the java plugin to generate jar files with a version
version = specificationVersion
group = modGroup

repositories {
    // location of the maven that hosts JEI files since January 2023
    maven("https://maven.blamejared.com")
    // location of a maven mirror for JEI files, as a fallback
    maven("https://modmaven.dev")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(modJavaVersion))
    }
}

base {
    archivesName = "${modId}-${minecraftVersion}"
}

minecraft {
    accessTransformers {
        file("src/main/resources/META-INF/accesstransformer.cfg")
    }
}

runs {
    create("client") {
        configure("client")
        systemProperty("forge.logging.console.level", "debug")
        workingDirectory(file("run/client/Dev"))
    }
    create("server") {
        configure("server")
        systemProperty("forge.logging.console.level", "debug")
        workingDirectory(file("run/server"))
        programArguments("nogui")
    }
}

// Sets up a dependency configuration called 'localRuntime'.
// This configuration should be used instead of 'runtimeOnly' to declare
// a dependency that will be present for runtime testing but that is
// "optional", meaning it will not be pulled by dependents of this mod.
val localRuntime = configurations.maybeCreate("localRuntime")

configurations {
    runtimeClasspath {
        extendsFrom(localRuntime.get())
    }
}

dependencies {
    implementation(
        group = "net.neoforged",
        name   = "neoforge",
        version = neoForgeVersion
    )
    compileOnly("mezz.jei:jei-${minecraftVersion}-neoforge-api:${jeiVersion}")
    localRuntime("mezz.jei:jei-${minecraftVersion}-neoforge:${jeiVersion}")

    // Hack fix for now, force jopt-simple to be exactly 5.0.4 because Mojang ships that version,
    // but some transitive dependencies request 6.0+
    implementation("net.sf.jopt-simple:jopt-simple:5.0.4") {
        version {
            strictly("5.0.4")
        }
    }
}

tasks.withType<Javadoc> {
    // workaround cast for https://github.com/gradle/gradle/issues/7038
    val standardJavadocDocletOptions = options as StandardJavadocDocletOptions
    // prevent java 8's strict doclint for javadocs from failing builds
    standardJavadocDocletOptions.addStringOption("Xdoclint:none", "-quiet")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(JavaLanguageVersion.of(modJavaVersion).asInt())
    javaToolchains {
        compilerFor {
            languageVersion.set(JavaLanguageVersion.of(modJavaVersion))
        }
    }
}

tasks.withType<Jar> {
    manifest {
        attributes(mapOf(
            "Specification-Title" to modName,
            "Specification-Vendor" to modAuthor,
            "Specification-Version" to specificationVersion,
            "Implementation-Title" to name,
            "Implementation-Version" to archiveVersion,
            "Implementation-Vendor" to modAuthor
        ))
    }
}

tasks.withType<ProcessResources> {
    // this will ensure that this task is redone when the versions change.
    val properties = mapOf(
        "neoForgeVersionRange" to neoForgeVersionRange,
        "githubUrl" to githubUrl,
        "loaderVersionRange" to loaderVersionRange,
        "minecraftVersion" to minecraftVersion,
        "minecraftVersionRange" to minecraftVersionRange,
        "modAuthor" to modAuthor,
        "modDescription" to modDescription,
        "modId" to modId,
        "modJavaVersion" to modJavaVersion,
        "modName" to modName,
        "jeiVersionRange" to jeiVersionRange,
        "version" to version,
    )
    properties.forEach { (key, value) ->
        inputs.property(key, value)
    }

    filesMatching(listOf("META-INF/neoforge.mods.toml", "pack.mcmeta")) {
        expand(properties)
    }
}

// Activate reproducible builds
// https://docs.gradle.org/current/userguide/working_with_files.html#sec:reproducible_archives
tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

// Merge the resources and classes into the same directory.
// This is done because java expects modules to be in a single directory.
// And if we have it in multiple we have to do performance intensive hacks like having the UnionFileSystem
// This will eventually be migrated to ForgeGradle so modders don't need to manually do it. But that is later.
sourceSets.forEach() {
    val dir = layout.buildDirectory.dir("sourcesSets/${it}.name")
    it.output.setResourcesDir(dir)
    it.java.destinationDirectory = dir
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
        for (fileName in listOf("run", "out", "logs")) {
            excludeDirs.add(file(fileName))
        }
    }
    project {
        jdkName = modJavaVersion
    }
}

subsystems {
    parchment {
        // The Minecraft version for which the Parchment mappings were created.
        // This does not necessarily need to match the Minecraft version your mod targets
        // Defaults to the value of Gradle property neogradle.subsystems.parchment.minecraftVersion
        minecraftVersion = parchmentMappingsMinecraftVersion

        // The version of Parchment mappings to apply.
        // See https://parchmentmc.org/docs/getting-started for a list.
        // Defaults to the value of Gradle property neogradle.subsystems.parchment.mappingsVersion
        mappingsVersion = parchmentMappingsVersion

        // Overrides the full Maven coordinate of the Parchment artifact to use
        // This is computed from the minecraftVersion and mappingsVersion properties by default.
        // If you set this property explicitly, minecraftVersion and mappingsVersion will be ignored.
        // The built-in default value can also be overriden using the Gradle property neogradle.subsystems.parchment.parchmentArtifact
        // parchmentArtifact = "org.parchmentmc.data:parchment-$minecraftVersion:$mappingsVersion:checked@zip"

        // Set this to false if you don't want the https://maven.parchmentmc.org/ repository to be added automatically when
        // applying Parchment mappings is enabled
        // The built-in default value can also be overriden using the Gradle property neogradle.subsystems.parchment.addRepository
        // addRepository = true

        // Can be used to explicitly disable this subsystem. By default, it will be enabled automatically as soon
        // as parchmentArtifact or minecraftVersion and mappingsVersion are set.
        // The built-in default value can also be overriden using the Gradle property neogradle.subsystems.parchment.enabled
        // enabled = true
    }
}
