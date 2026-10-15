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
        // Before 1.20.5 Forge's config API (also on Fabric through Forge Config API Port) has the same shape under another name.
        string(current.parsed < "1.20.5") {
            replace("ModConfigSpec", "ForgeConfigSpec")
        }
        // Item tooltips got a context object in 1.20.5; before that they took the level.
        string(current.parsed < "1.20.5") {
            replace("ItemStack stack, TooltipContext context,", "ItemStack stack, net.minecraft.world.level.Level context,")
        }
        // Crafting recipes read a CraftingInput and get registry lookups since 1.21.
        string(current.parsed < "1.21") {
            replace("CraftingInput input", "CraftingContainer input")
        }
        string(current.parsed < "1.21") {
            replace("input.items()", "input.getItems()")
        }
        string(current.parsed < "1.21") {
            replace("input.size()", "input.getContainerSize()")
        }
        string(current.parsed < "1.21") {
            replace("HolderLookup.Provider registries", "RegistryAccess registries")
        }
        // HUD layers got a DeltaTracker in 1.21; before that they took the partial tick.
        string(current.parsed < "1.21") {
            replace("DeltaTracker delta", "float delta")
        }
        // GeckoLib for these versions still keeps its animation classes in a "core" package.
        string(current.parsed < "1.20.5") {
            replace("software.bernie.geckolib.animatable.instance.", "software.bernie.geckolib.core.animatable.instance.")
        }
        string(current.parsed < "1.20.5") {
            replace("software.bernie.geckolib.animation.", "software.bernie.geckolib.core.animation.")
        }
        string(current.parsed >= "1.21.11") {
            replace("ResourceLocation", "Identifier")
        }
    }
}
