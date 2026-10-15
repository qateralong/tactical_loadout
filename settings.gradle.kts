pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.8"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

stonecutter {
    create(rootProject) {
        // Creates `versions/{minecraft}-{loader}` nodes, each built by `build.{loader}.gradle.kts`.
        fun match(version: String, vararg loaders: String) {
            for (loader in loaders) version("$version-$loader", version).buildscript("build.$loader.gradle.kts")
        }

        // Planned matrix: 1.20.1 forge/fabric; 1.21.1, 1.21.4, 1.21.8, 1.21.11 neoforge/fabric/forge.
        match("1.21.1", "neoforge")
        vcsVersion = "1.21.1-neoforge"
    }
}

rootProject.name = "tactical_loadout"
