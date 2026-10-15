plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "1.21.1-neoforge"

stonecutter parameters {
    val (version, loader) = current.project.split('-', limit = 2)

    // Applies version- and loader-specific tables from `stonecutter.properties.toml`.
    properties {
        tags(version, loader)
    }

    // Enables `//? if neoforge {` style conditions in sources.
    constants {
        match(loader, "fabric", "forge", "neoforge")
    }

    replacements {
        string(current.parsed >= "1.21.11") {
            replace("ResourceLocation", "Identifier")
        }
    }
}
