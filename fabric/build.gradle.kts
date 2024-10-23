plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

architectury {
    platformSetupLoomIde()
    fabric()
}

sourceSets {
    create("gametest") {
        compileClasspath += sourceSets["main"].compileClasspath
        runtimeClasspath += sourceSets["main"].runtimeClasspath
        compileClasspath += sourceSets["main"].output
        runtimeClasspath += sourceSets["main"].output
    }
}

loom {
    accessWidenerPath.set(project(":common").loom.accessWidenerPath)

    runs {
        create("gametest") {
            server()
            name = "Game Test"
            property("fabric-api.gametest", "true")
            runDir = "run/gametest"
            source("gametest")
        }

        create("gametestClient") {
            client()
            name = "Game Test Client"
            source("gametest")
        }
    }
}

base {
    archivesName.set("${rootProject.property("archives_base_name")}-fabric")
}

configurations {
    create("common")
    create("shadowCommon") // Don't use shadow from the shadow plugin since it *excludes* files.
    compileClasspath.get().extendsFrom(configurations["common"])
    runtimeClasspath.get().extendsFrom(configurations["common"])
    named("developmentFabric") {
        extendsFrom(configurations["common"])
    }
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${rootProject.property("fabric_loader_version")}")
    modApi("net.fabricmc.fabric-api:fabric-api:${rootProject.property("fabric_api_version")}")
    modApi("dev.architectury:architectury-fabric:${rootProject.property("architectury_version")}")
    modApi("com.terraformersmc:modmenu:${rootProject.property("modmenu_version")}")
    modApi("me.shedaniel.cloth:cloth-config-fabric:${rootProject.property("cloth_config_version")}")
    if (rootProject.hasProperty("with_mcef") && rootProject.property("with_mcef") == "true") {
        modRuntimeOnly("maven.modrinth:mcbrowser:${rootProject.property("mcbrowser_version")}")
        modRuntimeOnly("com.cinemamod:mcef-fabric:${rootProject.property("mcef_version")}")
    }
    if (rootProject.hasProperty("with_rei") && rootProject.property("with_rei") == "true") {
        modRuntimeOnly("me.shedaniel:RoughlyEnoughItems-fabric:${rootProject.property("rei_version")}")
    }
    if (rootProject.hasProperty("with_emi") && rootProject.property("with_emi") == "true") {
        modRuntimeOnly("dev.emi:emi-fabric:${rootProject.property("emi_version")}")
    }
    if (rootProject.hasProperty("with_jei") && rootProject.property("with_jei") == "true") {
        modRuntimeOnly("mezz.jei:jei-${rootProject.property("jei_minecraft_version")}-fabric:${rootProject.property("jei_version")}")
    }

    "common"(project(":common", "namedElements")) { isTransitive = false }
    "shadowCommon"(project(":common", "transformProductionFabric")) { isTransitive = false }
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
    }

    shadowJar {
        exclude("architectury.common.json")
        configurations = listOf(project.configurations["shadowCommon"])
        archiveClassifier.set("dev-shadow")
    }

    remapJar {
        injectAccessWidener.set(true)
        inputFile.set(shadowJar.get().archiveFile)
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
        create<MavenPublication>("mavenFabric") {
            artifactId = "${rootProject.property("archives_base_name")}-${project.name}"
            from(components["java"])
        }
    }

    repositories {
        // Add repositories to publish to here.
    }
}