package tacz_camo_armor;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.DispenserBlock;
import org.slf4j.Logger;
import tacz_camo_armor.grenade.GrenadeKind;
import tacz_camo_armor.registry.ModArmorMaterials;
import tacz_camo_armor.registry.ModCreativeTabs;
import tacz_camo_armor.registry.ModDataComponents;
import tacz_camo_armor.registry.ModEntities;
import tacz_camo_armor.registry.ModItems;
import tacz_camo_armor.registry.ModParticles;
import tacz_camo_armor.registry.ModRecipes;
import tacz_camo_armor.registry.ModSounds;

/** Loader-independent entry point; each loader calls {@link #init} first and {@link #commonSetup} once registries are ready. */
public final class TaczCamoArmor {
    public static final String MOD_ID = "tacz_camo_armor";
    public static final Logger LOGGER = LogUtils.getLogger();

    private TaczCamoArmor() {
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
