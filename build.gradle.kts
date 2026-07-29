plugins {
    id("java")
    id("idea")
    id("eclipse")
    // https://projects.neoforged.net/neoforged/moddevgradle
    id("net.neoforged.moddev") version("2.0.143")
}

// gradle.properties
val neoForgeVersion = providers.gradleProperty("neoForgeVersion").get()
val neoForgeVersionRange = providers.gradleProperty("neoForgeVersionRange").get()
val githubUrl = providers.gradleProperty("githubUrl").get()
val minecraftVersion = providers.gradleProperty("minecraftVersion").get()
val minecraftVersionRange = providers.gradleProperty("minecraftVersionRange").get()
val modAuthor = providers.gradleProperty("modAuthor").get()
val modDescription = providers.gradleProperty("modDescription").get()
val modGroup = providers.gradleProperty("modGroup").get()
val modId = providers.gradleProperty("modId").get()
val modName = providers.gradleProperty("modName").get()
val modJavaVersion = providers.gradleProperty("modJavaVersion").get()
val specificationVersion = providers.gradleProperty("specificationVersion").get()
val jeiVersion = providers.gradleProperty("jeiVersion").get()
val jeiVersionRange = providers.gradleProperty("jeiVersionRange").get()
val loaderVersionRange = providers.gradleProperty("loaderVersionRange").get()

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

neoForge {
    version = neoForgeVersion
    validateAccessTransformers.set(true)

    runs {
        create("client") {
            client()
            logLevel.set(org.slf4j.event.Level.DEBUG)
            systemProperty("forge.logging.console.level", "debug")
            gameDirectory.set(layout.projectDirectory.dir("run/client/Dev"))
        }
        create("server") {
            server()
            logLevel.set(org.slf4j.event.Level.DEBUG)
            systemProperty("forge.logging.console.level", "debug")
            gameDirectory.set(layout.projectDirectory.dir("run/server"))
            programArgument("nogui")
        }
    }

    mods {
        create(modId) {
            sourceSet(sourceSets.main.get())
        }
    }
}

// Sets up a dependency configuration called 'localRuntime'.
// This configuration should be used instead of 'runtimeOnly' to declare
// a dependency that will be present for runtime testing but that is
// "optional", meaning it will not be pulled by dependents of this mod.
val localRuntime = configurations.create("localRuntime")

configurations {
    runtimeClasspath {
        extendsFrom(localRuntime)
    }
}

dependencies {
    compileOnly("mezz.jei:jei-${minecraftVersion}-neoforge-api:${jeiVersion}")
    add(localRuntime.name, "mezz.jei:jei-${minecraftVersion}-neoforge:${jeiVersion}")

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
