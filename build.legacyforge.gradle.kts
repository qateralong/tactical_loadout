plugins {
    id("net.neoforged.moddev.legacyforge") version "2.0.148"
    id("neoforge-mutex")
}

val modId = property("mod.id") as String
val mcVersion = sc.current.version
version = "${property("mod.version")}+$mcVersion"
group = property("mod.group") as String
base.archivesName = "$modId-forge"

java {
    targetCompatibility = JavaVersion.VERSION_17
    sourceCompatibility = JavaVersion.VERSION_17
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}

// Forge before 1.20.5 has its own loader package; the newer Forge, NeoForge and Fabric code stays out.
sourceSets.main {
    java {
        exclude("$modId/fabric/**", "$modId/forge/**", "$modId/neoforge/**", "$modId/datagen/**")
    }
    resources {
        srcDir(rootProject.file("src/generated/resources"))
        // Data packs use the old folder layout here, converted by tools/backport_data.py and added in processResources.
        exclude("**/*.bbmodel", ".cache/**", "data/**", "$modId.fabric.mixins.json")
    }
}

legacyForge {
    version = "$mcVersion-${property("deps.forge_loader")}"
    parchment {
        minecraftVersion = property("deps.parchment_mc") as String
        mappingsVersion = property("deps.parchment") as String
    }
    runs {
        configureEach {
            gameDirectory = rootProject.layout.projectDirectory.dir("run/legacyforge")
        }
        register("client") {
            client()
        }
        register("server") {
            server()
            programArgument("--nogui")
        }
    }
    mods {
        register(modId) {
            sourceSet(sourceSets.main.get())
        }
    }
}

mixin {
    add(sourceSets.main.get(), "$modId.refmap.json")
    config("$modId.mixins.json")
    config("$modId.armor.mixins.json")
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
    exclusiveContent {
        forRepository { maven("https://api.modrinth.com/maven") { name = "Modrinth" } }
        filter { includeGroup("maven.modrinth") }
    }
    maven("https://maven.blamejared.com") {
        name = "BlameJared"
        content {
            includeGroup("mezz.jei")
            includeGroupByRegex("net\\.mezzdev.*")
        }
    }
}

dependencies {
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    modImplementation("software.bernie.geckolib:geckolib-forge-$mcVersion:${property("deps.geckolib")}")
    modImplementation(property("deps.tacz") as String)
    modCompileOnly("mezz.jei:jei-$mcVersion-common-api:${property("deps.jei")}") { isTransitive = false }
    modCompileOnly("mezz.jei:jei-$mcVersion-forge-api:${property("deps.jei")}") { isTransitive = false }
    modRuntimeOnly("mezz.jei:jei-$mcVersion-forge:${property("deps.jei")}") { isTransitive = false }
    modRuntimeOnly("net.mezzdev.config:mezz_config-$mcVersion-forge:${property("deps.mezz_config")}")
}

val metadataProps = mapOf(
    "mod_id" to modId,
    "mod_name" to property("mod.name"),
    "mod_license" to property("mod.license"),
    "mod_version" to version.toString(),
    "minecraft_version_range" to property("mod.mc_compat"),
    "forge_major" to (property("deps.forge_loader") as String).substringBefore('.'),
    "forge_version_range" to property("deps.forge_compat"),
    "geckolib_version" to property("deps.geckolib"),
    "jei_major" to (property("deps.jei") as String).substringBefore('.'),
    "resource_pack_format" to property("mod.resource_pack_format"),
    "data_pack_format" to property("mod.data_pack_format"),
    "config_dependency" to "",
)

val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
    inputs.properties(metadataProps)
    expand(metadataProps)
    from(rootProject.file("src/main/templates/forge"))
    into(layout.buildDirectory.dir("generated/sources/modMetadata"))
}
sourceSets.main.get().resources.srcDir(generateModMetadata)
legacyForge.ideSyncTask(generateModMetadata)

tasks {
    processResources {
        inputs.property("mixin_java", "JAVA_17")
        val refmap = "$modId.refmap.json"
        // Armor wear and the HUD work differently before 1.21, so a few mixins only join the configs here.
        val extraMixins = mapOf("LivingEntityArmorMixin" to "InventoryArmorMixin", "MinecraftMixin" to "GameRendererMixin")
        inputs.property("extra_mixins", extraMixins)
        filesMatching("*.mixins.json") {
            expand("java" to "JAVA_17")
            // SRG-named production needs the refmap written by the mixin annotation processor.
            filter { line -> line.replace("\"compatibilityLevel\"", "\"refmap\": \"$refmap\",\n  \"compatibilityLevel\"") }
            for ((anchor, extra) in extraMixins) {
                filter { line -> line.replace("\"$anchor\"", "\"$anchor\",\n    \"$extra\"") }
            }
        }
        from(rootProject.file("src/generated/resources-1.20.1"))
    }

    jar {
        val licenseName = "LICENSE_$modId"
        from(rootProject.file("LICENSE")) { rename { licenseName } }
        manifest.attributes("MixinConfigs" to "$modId.mixins.json,$modId.armor.mixins.json")
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }
}
