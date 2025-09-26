architectury {
    if (rootProject.property("snapshot") == "false") {
        common(rootProject.property("enabled_platforms").toString().split(","))
    } else {
        common(rootProject.property("enabled_platforms_snapshot").toString().split(","))
    }
}

loom {
    accessWidenerPath.set(file("src/main/resources/heywiki.accesswidener"))
}

dependencies {
    // We depend on fabric loader here to use the fabric @Environment annotations and get the mixin dependencies
    // Do NOT use other classes from fabric loader
    modImplementation("net.fabricmc:fabric-loader:${rootProject.property("fabric_loader_version")}")
    // Remove the next line if you don't want to depend on the API
    modApi("dev.architectury:architectury:${rootProject.property("architectury_version")}")
    modApi("me.shedaniel.cloth:cloth-config:${rootProject.property("cloth_config_version")}")
    modCompileOnlyApi("me.shedaniel:RoughlyEnoughItems-api:${rootProject.property("rei_version")}")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-runtime-fabric:${rootProject.property("rei_version")}")
    modCompileOnly("dev.emi:emi-xplat-intermediary:${rootProject.property("emi_version")}")
    modCompileOnly("mezz.jei:jei-${rootProject.property("jei_minecraft_version")}-fabric:${rootProject.property("jei_version")}") {
        exclude(group = "mezz.jei")
        isTransitive = false
    }

    implementation("com.google.code.gson:gson:2.10.1")
}

publishing {
    publications {
        create<MavenPublication>("mavenCommon") {
            artifactId = rootProject.property("archives_base_name").toString()
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
    }
}