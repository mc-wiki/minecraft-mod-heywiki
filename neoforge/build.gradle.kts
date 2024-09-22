plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

loom {
    accessWidenerPath.set(project(":common").loom.accessWidenerPath)
}

base {
    archivesName.set("${rootProject.property("archives_base_name")}-neoforge")
}

configurations {
    create("common")
    create("shadowCommon") // Don't use shadow from the shadow plugin since it *excludes* files.
    compileClasspath.get().extendsFrom(configurations["common"])
    runtimeClasspath.get().extendsFrom(configurations["common"])
    named("developmentNeoForge") {
        extendsFrom(configurations["common"])
    }
}

dependencies {
    "neoForge"("net.neoforged:neoforge:${rootProject.property("neoforge_version")}")
    modApi("dev.architectury:architectury-neoforge:${rootProject.property("architectury_version")}")
    modApi("me.shedaniel.cloth:cloth-config-neoforge:${rootProject.property("cloth_config_version")}")
    if (rootProject.hasProperty("with_rei") && rootProject.property("with_rei") == "true") {
        modRuntimeOnly("me.shedaniel:RoughlyEnoughItems-neoforge:${rootProject.property("rei_version")}")
    }
    if (rootProject.hasProperty("with_emi") && rootProject.property("with_emi") == "true") {
        modRuntimeOnly("dev.emi:emi-neoforge:${rootProject.property("emi_version")}")
    }
    if (rootProject.hasProperty("with_jei") && rootProject.property("with_jei") == "true") {
        modRuntimeOnly("mezz.jei:jei-${rootProject.property("jei_minecraft_version")}-neoforge:${rootProject.property("jei_version")}")
    }

    "common"(project(":common", "namedElements")) { isTransitive = false }
    "shadowCommon"(project(":common", "transformProductionNeoForge")) { isTransitive = false }
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("META-INF/neoforge.mods.toml") {
            expand("version" to project.version)
        }
    }

    shadowJar {
        exclude("fabric.mod.json")
        exclude("architectury.common.json")
        configurations = listOf(project.configurations["shadowCommon"])
        archiveClassifier.set("dev-shadow")
    }

    remapJar {
        inputFile.set(shadowJar.flatMap { it.archiveFile })
        dependsOn(shadowJar)
    }

    sourcesJar {
        val commonSources = project(":common").tasks.getByName<Jar>("sourcesJar")
        dependsOn(commonSources)
        from(commonSources.archiveFile.map { zipTree(it) })
    }
}

components.getByName<AdhocComponentWithVariants>("java").withVariantsFromConfiguration(configurations["sourcesElements"]) {
    skip()
}

publishing {
    publications {
        create<MavenPublication>("mavenforge") {
            artifactId = "${rootProject.property("archives_base_name")}-${project.name}"
            from(components["java"])
        }
    }

    repositories {
        // Add repositories to publish to here.
    }
}