import kotlin.math.exp

plugins {
    `java-library`

    alias(libs.plugins.fabric.loom) apply false
    alias(libs.plugins.neogradle) apply false

    alias(libs.plugins.modstitch.multiloader)
    alias(libs.plugins.modstitch.manifests)
    alias(libs.plugins.modstitch.accessx)

    alias(libs.plugins.mod.publish.plugin)
    `maven-publish`
}

val minecraftVersion: String = libs.versions.minecraft.get()

group = "wiki.minecraft.heywiki"
version = "1.9.0+$minecraftVersion"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}


repositories {
    maven {
        name = "Xander Maven"
        url = uri("https://maven.isxander.dev/releases")
    }

    maven {
        name = "Terraformers Maven"
        url = uri("https://maven.terraformersmc.com/releases")
    }

    maven {
        name = "Neoforged Maven"
        url = uri("https://maven.neoforged.net/releases")
    }

    maven { url = uri("https://maven.shedaniel.me/") }

    maven { url = uri("https://maven.blamejared.com/") }

    maven { url = uri("https://mcef-download.cinemamod.com/repositories/releases") }

    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven")
            }
        }

        filter {
            includeGroup("maven.modrinth")
        }
    }
}

dependencies {
    minecraft(libs.minecraft)
    // modstitch-multiloader: provides loader dependencies like mixin and mixin extras to common source set
    fabricLoader(libs.fabric.loader)
    neoforgeImplementation(libs.neoforge)

    commonImplementation(libs.cloth.config)
//    compileOnlyApi("me.shedaniel:RoughlyEnoughItems-api:${rootProject.property("rei_version")}")
//    compileOnly("me.shedaniel:RoughlyEnoughItems-runtime-fabric:${rootProject.property("rei_version")}")
//    compileOnly("dev.emi:emi-xplat-intermediary:${rootProject.property("emi_version")}")
    commonCompileOnly(libs.jei.fabric) {
        exclude(group = "mezz.jei")
        isTransitive = false
    }

    commonImplementation(libs.gson)

    fabricImplementation(platform(libs.fabric.api.bom))
    fabricImplementation(libs.fabric.api.key.mapping.api.v1)
    fabricImplementation(libs.fabric.api.resource.loader.v1)
    fabricImplementation(libs.fabric.api.command.api.v2)
    fabricImplementation(libs.fabric.api.lifecycle.events.v1)
    fabricImplementation(libs.fabric.api.message.api.v1)
    fabricImplementation(libs.fabric.api.item.api.v1)
    fabricRuntimeOnly(libs.fabric.api)

    fabricImplementation(libs.mod.menu)

    fabricApi(libs.mod.menu)
    fabricApi(libs.cloth.config.fabric)
//    if (rootProject.hasProperty("with_mcef") && rootProject.property("with_mcef") == "true") {
//        runtimeOnly("maven.modrinth:mcbrowser:${rootProject.property("mcbrowser_version")}")
//        runtimeOnly("com.cinemamod:mcef-fabric:${rootProject.property("mcef_version")}")
//    }
//    if (rootProject.hasProperty("with_rei") && rootProject.property("with_rei") == "true") {
//        runtimeOnly("me.shedaniel:RoughlyEnoughItems-fabric:${rootProject.property("rei_version")}")
//    }
//    if (rootProject.hasProperty("with_emi") && rootProject.property("with_emi") == "true") {
//        runtimeOnly("dev.emi:emi-fabric:${rootProject.property("emi_version")}")
//    }
    if (rootProject.hasProperty("with_jei") && rootProject.property("with_jei") == "true") {
        fabricRuntimeOnly(libs.jei.fabric)
        neoforgeRuntimeOnly(libs.jei.neoforge)
    }

    neoforgeApi(libs.cloth.config.neoforge)
//    if (rootProject.hasProperty("with_rei") && rootProject.property("with_rei") == "true") {
//        runtimeOnly("me.shedaniel:RoughlyEnoughItems-neoforge:${rootProject.property("rei_version")}")
//    }
//    if (rootProject.hasProperty("with_emi") && rootProject.property("with_emi") == "true") {
//        runtimeOnly("dev.emi:emi-neoforge:${rootProject.property("emi_version")}")
//    }
}

val canonicalAW = layout.projectDirectory.file("heywiki.accesswidener")

// Loom does everything at configuration time which means accessx cannot be used
// to source comptime access wideners
loom.accessWidenerPath = canonicalAW

val fabricAWTask = accessx.convert("fabric", sourceSets.fabric.name) {
    inputFiles.from(canonicalAW)
    outputFormat = accessx.AW_V1
}
// modstitch-accessx: converts the access widener into an access transformer and includes it in resources
val neoforgeAWTask = accessx.convert("neoforge", sourceSets.neoforge.name) {
    inputFiles.from(canonicalAW)
    outputFormat = accessx.AT
}
// access transformers must be sourced BEFORE `neoforgeImplementation(libs.neoforge)`
accessTransformers.files.from(neoforgeAWTask.flatMap { it.outputFile })
// NeoGradle bug: https://github.com/neoforged/NeoGradle/issues/318
tasks.named { it in listOf("neoFormTransformSource", "applyAccessTransformer") }.configureEach {
    dependsOn(neoforgeAWTask)
}

val minecraftConstraint = "[26.1,26.2)"
val minecraftConstraintFabric = "~26.1"
val supportedMinecraftVersions = manifests.minecraftReleasesMatching(minecraftConstraint)

tasks.withType<Jar>().configureEach {
    from(rootProject.file("LICENSE")) {
        into("META-INF")
    }
}

tasks.withType<Copy>().configureEach {
    filesMatching("**/fabric.mod.json") {
        expand(
            "version" to project.version.toString(),
            "minecraftVersion" to minecraftConstraintFabric
        )
    }
    filesMatching("**/META-INF/neoforge.mods.toml") {
        expand(
            "version" to project.version.toString(),
            "minecraftVersion" to minecraftConstraint
        )
    }
}

publishMods {
    changelog = providers.fileContents(layout.projectDirectory.file("CHANGELOG.md")).asText
    type = STABLE

    val fabricOptions = publishOptions {
        file = tasks.fabricJar.flatMap { it.archiveFile }
        additionalFiles.from(tasks.fabricSourcesJar.flatMap { it.archiveFile })
        modLoaders.add("fabric")
    }
    val neoforgeOptions = publishOptions {
        file = tasks.neoforgeJar.flatMap { it.archiveFile }
        additionalFiles.from(tasks.neoforgeSourcesJar.flatMap { it.archiveFile })
        modLoaders.add("neoforge")
    }
    val modrinthOptions = modrinthOptions {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = providers.gradleProperty("6DnswkCZ")
        minecraftVersions = supportedMinecraftVersions
    }
    val curseforgeOptions = curseforgeOptions {
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        projectId = providers.gradleProperty("997027")
        minecraftVersions = supportedMinecraftVersions
    }
    val githubOptions = githubOptions {
        accessToken = providers.environmentVariable("GITHUB_TOKEN")
        repository.set("mc-wiki/minecraft-mod-heywiki")
        commitish.set(providers.environmentVariable("BRANCH"))
    }
    modrinth("modrinthFabric") {
        from(modrinthOptions, fabricOptions)
    }
    modrinth("modrinthNeoforge") {
        from(modrinthOptions, neoforgeOptions)
    }
    curseforge("curseforgeFabric") {
        from(curseforgeOptions, fabricOptions)
    }
    curseforge("curseforgeNeoforge") {
        from(curseforgeOptions, neoforgeOptions)
    }
    github("github") {
        from(githubOptions, publishOptions {
            additionalFiles.from(
                tasks.fabricJar.flatMap { it.archiveFile },
                tasks.fabricSourcesJar.flatMap { it.archiveFile },
                tasks.neoforgeJar.flatMap { it.archiveFile },
                tasks.neoforgeSourcesJar.flatMap { it.archiveFile }
            )
        })
    }

}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])

            artifact(tasks.universalJar)
            artifact(tasks.universalSourcesJar)
        }
    }

    repositories {
        mavenLocal()
    }
}
