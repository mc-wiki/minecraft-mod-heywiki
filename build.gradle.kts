@file:Suppress("UnstableApiUsage")

plugins {
    id("architectury-plugin") version "3.4-SNAPSHOT"
    id("dev.architectury.loom") version "1.7-SNAPSHOT" apply false
    java
}

architectury {
    minecraft = rootProject.property("minecraft_version") as String
}

subprojects {
    apply(plugin = "dev.architectury.loom")

    configure<net.fabricmc.loom.api.LoomGradleExtensionAPI> {
        silentMojangMappingsLicense()

        dependencies {
            "minecraft"("com.mojang:minecraft:${rootProject.property("minecraft_version")}")
            "mappings"(layered {
                mappings("net.fabricmc:yarn:${rootProject.property("yarn_mappings")}:v2")
                mappings("dev.architectury:yarn-mappings-patch-neoforge:1.21+build.4")
            })
        }
    }
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "architectury-plugin")
    apply(plugin = "maven-publish")

    version = rootProject.property("mod_version") as String
    group = rootProject.property("maven_group") as String

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

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    java {
        withSourcesJar()
    }

    tasks.javadoc {
        exclude("**/mixin/**")
    }
}