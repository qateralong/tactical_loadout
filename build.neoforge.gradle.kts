plugins {
    id("net.neoforged.moddev") version "2.0.148"
    id("neoforge-mutex")
}

val modId = property("mod.id") as String
val minecraft = sc.current.version
version = "${property("mod.version")}+$minecraft"
group = property("mod.group") as String
base.archivesName = "$modId-neoforge"

val requiredJava = when {
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    else -> JavaVersion.VERSION_17
}

java {
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava
    toolchain.languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
}

// Loader-specific code lives in its own package; the other loaders' packages are left out of this build.
sourceSets.main {
    java {
        exclude("$modId/fabric/**", "$modId/forge/**")
    }
    resources {
        srcDir(rootProject.file("src/generated/resources"))
        exclude("**/*.bbmodel", ".cache/**")
    }
}

repositories {
    maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/") {
        name = "GeckoLib"
        content {
            includeGroupByRegex("software\\.bernie.*")
            includeGroup("com.eliotlash.mclib")
        }
    }
    maven("https://maven.blamejared.com") {
        name = "BlameJared"
        content {
            includeGroup("mezz.jei")
            includeGroupByRegex("net\\.mezzdev.*")
        }
    }
    exclusiveContent {
        forRepository { maven("https://api.modrinth.com/maven") { name = "Modrinth" } }
        filter { includeGroup("maven.modrinth") }
    }
}

dependencies {
    implementation("software.bernie.geckolib:geckolib-neoforge-$minecraft:${property("deps.geckolib")}")
    implementation(property("deps.tacz") as String)
    compileOnly("mezz.jei:jei-$minecraft-neoforge-api:${property("deps.jei")}")
    runtimeOnly("mezz.jei:jei-$minecraft-neoforge:${property("deps.jei")}")
}

neoForge {
    version = property("deps.neo_loader") as String

    parchment {
        mappingsVersion = property("deps.parchment") as String
        minecraftVersion = property("deps.parchment_mc") as String
    }

    runs {
        register("client") {
            client()
            gameDirectory = rootProject.file("run")
        }
        register("server") {
            server()
            gameDirectory = rootProject.file("run")
            programArgument("--nogui")
        }
        register("data") {
            data()
            gameDirectory = rootProject.file("run")
            programArguments.addAll(
                "--mod", modId, "--all",
                "--output", rootProject.file("src/generated/resources").absolutePath,
                "--existing", rootProject.file("src/main/resources").absolutePath,
            )
        }
        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            logLevel = org.slf4j.event.Level.DEBUG
        }
    }

    mods {
        register(modId) {
            sourceSet(sourceSets.main.get())
        }
    }
}

val metadataProps = mapOf(
    "mod_id" to modId,
    "mod_name" to property("mod.name"),
    "mod_license" to property("mod.license"),
    "mod_version" to version.toString(),
    "minecraft_version_range" to property("mod.mc_compat"),
    "neo_version_range" to property("deps.neo_compat"),
    "loader_version_range" to "[1,)",
    "geckolib_version" to property("deps.geckolib"),
)

val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
    inputs.properties(metadataProps)
    expand(metadataProps)
    from(rootProject.file("src/main/templates/neoforge"))
    into(layout.buildDirectory.dir("generated/sources/modMetadata"))
}
sourceSets.main.get().resources.srcDir(generateModMetadata)
neoForge.ideSyncTask(generateModMetadata)

tasks {
    processResources {
        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        inputs.property("mixin_java", mixinJava)
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }
        exclude("fabric.mod.json")
    }

    jar {
        val licenseName = "LICENSE_$modId"
        from(rootProject.file("LICENSE")) { rename { licenseName } }
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }
}
