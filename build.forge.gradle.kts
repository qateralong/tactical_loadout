plugins {
    id("net.minecraftforge.gradle") version "7.0.40"
}

val modId = property("mod.id") as String
val mcVersion = sc.current.version
version = "${property("mod.version")}+$mcVersion"
group = property("mod.group") as String
base.archivesName = "$modId-forge"

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
        exclude("$modId/fabric/**", "$modId/neoforge/**", "$modId/legacyforge/**", "$modId/datagen/**")
    }
    resources {
        srcDir(rootProject.file("src/generated/resources"))
        exclude("**/*.bbmodel", ".cache/**")
    }
}

// Forge's dev launcher treats each output directory as its own mod file, so classes and resources share one.
sourceSets.main.get().output.setResourcesDir(sourceSets.main.get().java.destinationDirectory)

minecraft {
    mappings("official", mcVersion)
    runs {
        configureEach {
            workingDir = rootProject.layout.projectDirectory.dir("run/forge")
            // Production jars list their mixins in the manifest; dev runs read classes directly and need them passed in.
            args("--mixin.config", "$modId.mixins.json", "--mixin.config", "$modId.armor.mixins.json")
        }
        register("client")
        register("server") {
            args("--nogui")
        }
    }
}

repositories {
    minecraft.mavenizer(this)
    maven(fg.forgeMaven)
    maven(fg.minecraftLibsMaven)
    mavenCentral()
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
    implementation(minecraft.dependency("net.minecraftforge:forge:$mcVersion-${property("deps.forge_loader")}"))
    implementation("software.bernie.geckolib:geckolib-forge-$mcVersion:${property("deps.geckolib")}")
    implementation("maven.modrinth:forge-config-api-port:${property("deps.fcap")}")
    compileOnly("mezz.jei:jei-$mcVersion-common-api:${property("deps.jei")}") { isTransitive = false }
}

val metadataProps = mapOf(
    "mod_id" to modId,
    "mod_name" to property("mod.name"),
    "mod_license" to property("mod.license"),
    "mod_version" to version.toString(),
    "minecraft_version_range" to property("mod.mc_compat"),
    "forge_major" to (property("deps.forge_loader") as String).substringBefore('.'),
    "forge_version_range" to property("deps.forge_compat"),
    "jei_major" to (property("deps.jei") as String).substringBefore('.'),
    // The shared config classes use NeoForge's config API, which Forge gets from Forge Config API Port.
    "config_dependency" to """

[[dependencies.$modId]]
    modId="forgeconfigapiport"
    mandatory=true
    versionRange="*"
    ordering="NONE"
    side="BOTH"
""",
    "geckolib_version" to property("deps.geckolib"),
    "resource_pack_format" to property("mod.resource_pack_format"),
    "data_pack_format" to property("mod.data_pack_format"),
)

val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
    inputs.properties(metadataProps)
    expand(metadataProps)
    from(rootProject.file("src/main/templates/forge"))
    into(layout.buildDirectory.dir("generated/sources/modMetadata"))
}
sourceSets.main.get().resources.srcDir(generateModMetadata)

tasks {
    processResources {
        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        inputs.property("mixin_java", mixinJava)
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }
        // NeoForge loot modifiers; Forge adds the same supply tables from code.
        exclude("$modId.fabric.mixins.json", "data/neoforge/**", "data/$modId/loot_modifiers/**")
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
