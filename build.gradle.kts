plugins {
    id("java")
    id("idea")
    id("eclipse")
    id("net.minecraftforge.gradle") version("[6.0,6.2)")
}

// gradle.properties
val forgeVersion: String by extra
val forgeVersionRange: String by extra
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
    mappings("official", minecraftVersion)

    copyIdeResources.set(true)

    accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))

    runs {
        create("client") {
            taskName("runClientDev")
            property("forge.logging.console.level", "debug")
            workingDirectory(file("run/client"))
            mods {
                create(modId) {
                    source(sourceSets.main.get())
                }
            }
        }
        create("server") {
            taskName("Server")
            property("forge.logging.console.level", "debug")
            workingDirectory(file("run/server"))
            mods {
                create(modId) {
                    source(sourceSets.main.get())
                }
            }
            args.add("--nogui")
        }
    }
}

dependencies {
    "minecraft"(
        group = "net.minecraftforge",
        name   = "forge",
        version = "${minecraftVersion}-${forgeVersion}"
    )
    compileOnly("mezz.jei:jei-${minecraftVersion}-common-api:${jeiVersion}")
    runtimeOnly("mezz.jei:jei-${minecraftVersion}-forge:${jeiVersion}")

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
    inputs.property("version", version)

    filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta")) {
        expand(mapOf(
            "forgeVersionRange" to forgeVersionRange,
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
        ))
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
