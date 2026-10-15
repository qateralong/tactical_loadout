package tactical_loadout;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.DispenserBlock;
import org.slf4j.Logger;
import tactical_loadout.grenade.GrenadeKind;
import tactical_loadout.registry.ModArmorMaterials;
import tactical_loadout.registry.ModCreativeTabs;
import tactical_loadout.registry.ModDataComponents;
import tactical_loadout.registry.ModEntities;
import tactical_loadout.registry.ModItems;
import tactical_loadout.registry.ModParticles;
import tactical_loadout.registry.ModRecipes;
import tactical_loadout.registry.ModSounds;

/** Loader-independent entry point; each loader calls {@link #init} first and {@link #commonSetup} once registries are ready. */
public final class TacticalLoadout {
    public static final String MOD_ID = "tactical_loadout";
    public static final Logger LOGGER = LogUtils.getLogger();

    private TacticalLoadout() {
    }

    public static void init() {
        ModArmorMaterials.init();
        ModDataComponents.init();
        ModItems.init();
        ModRecipes.init();
        ModEntities.init();
        ModParticles.init();
        ModSounds.init();
        ModCreativeTabs.init();
    }

    public static void commonSetup() {
        for (GrenadeKind kind : GrenadeKind.values()) {
            DispenserBlock.registerProjectileBehavior(ModItems.grenade(kind).get());
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
