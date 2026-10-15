plugins {
    id("fabric-loom") version "1.18.4"
}

val modId = property("mod.id") as String
val minecraft = sc.current.version
version = "${property("mod.version")}+$minecraft"
group = property("mod.group") as String
base.archivesName = "$modId-fabric"

val requiredJava = when {
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    else -> JavaVersion.VERSION_17
}

java {
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava
    toolchain.languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
}

// Loader-specific code lives in its own package; the other loaders' packages and the NeoForge data generators stay out.
sourceSets.main {
    java {
        exclude("$modId/neoforge/**", "$modId/forge/**", "$modId/legacyforge/**", "$modId/datagen/**")
    }
    resources {
        srcDir(rootProject.file("src/generated/resources"))
        exclude("**/*.bbmodel", ".cache/**")
        // Before 1.21 data packs use the old folder layout, converted by tools/backport_data.py and added in processResources.
        if (sc.current.parsed < "1.21") exclude("data/**")
    }
}

repositories {
    maven("https://maven.parchmentmc.org") {
        name = "ParchmentMC"
        content { includeGroup("org.parchmentmc.data") }
    }
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
    minecraft("com.mojang:minecraft:$minecraft")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${property("deps.parchment_mc")}:${property("deps.parchment")}@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
    modImplementation("software.bernie.geckolib:geckolib-fabric-$minecraft:${property("deps.geckolib")}")
    modImplementation("maven.modrinth:forge-config-api-port:${property("deps.fcap")}")
    // The config port ships Night Config nested in its jar, which the dev runtime does not unpack.
    implementation("com.electronwill.night-config:core:3.8.0")
    implementation("com.electronwill.night-config:toml:3.8.0")
    modCompileOnly("mezz.jei:jei-$minecraft-fabric-api:${property("deps.jei")}")
    modRuntimeOnly("mezz.jei:jei-$minecraft-fabric:${property("deps.jei")}")
    if (hasProperty("deps.mezz_config")) {
        modRuntimeOnly("net.mezzdev.config:mezz_config-$minecraft-fabric:${property("deps.mezz_config")}")
    }
}

loom {
    runs.configureEach {
        runDir = if (sc.current.parsed >= "1.21") "../../run/fabric" else "../../run/fabric-${sc.current.version}"
        ideConfigGenerated(false)
    }
}

val metadataProps = mapOf(
    "mod_id" to modId,
    "mod_name" to property("mod.name"),
    "mod_license" to property("mod.license"),
    "mod_version" to version.toString(),
    "minecraft_version_range" to property("mod.mc_compat"),
    "java_version" to requiredJava.majorVersion,
    "geckolib_version" to property("deps.geckolib"),
)

val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
    inputs.properties(metadataProps)
    expand(metadataProps)
    from(rootProject.file("src/main/templates/fabric"))
    into(layout.buildDirectory.dir("generated/sources/modMetadata"))
}
sourceSets.main.get().resources.srcDir(generateModMetadata)

tasks {
    processResources {
        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        inputs.property("mixin_java", mixinJava)
        // Before 1.21 armor wear and the HUD work differently, so a few mixins only join the configs there.
        val extraMixins = if (sc.current.parsed < "1.21") {
            mapOf("LivingEntityArmorMixin" to "InventoryArmorMixin,LivingEntityJumpMixin", "MinecraftMixin" to "GameRendererMixin")
        } else {
            emptyMap()
        }
        inputs.property("extra_mixins", extraMixins)
        filesMatching("*.mixins.json") {
            expand("java" to mixinJava)
            for ((anchor, extra) in extraMixins) {
                filter { line -> line.replace("\"$anchor\"", (listOf(anchor) + extra.split(",")).joinToString(",\n    ") { "\"$it\"" }) }
            }
        }
        // NeoForge loot modifiers; Fabric adds the same supply tables from code.
        exclude("data/neoforge/**", "data/$modId/loot_modifiers/**")
        if (sc.current.parsed < "1.21") from(rootProject.file("src/generated/resources-1.20.1"))
    }

    jar {
        val licenseName = "LICENSE_$modId"
        from(rootProject.file("LICENSE")) { rename { licenseName } }
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }
}
