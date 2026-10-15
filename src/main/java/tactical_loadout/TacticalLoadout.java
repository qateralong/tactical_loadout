package tactical_loadout;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.DispenserBlock;
import org.slf4j.Logger;
import tactical_loadout.grenade.GrenadeKind;
//? if <1.21 {
/*import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import tactical_loadout.grenade.GrenadeEntity;
*///?}
import tactical_loadout.registry.ModArmorMaterials;
import tactical_loadout.registry.ModBlocks;
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
        ModBlocks.init();
        ModItems.init();
        ModRecipes.init();
        ModEntities.init();
        ModParticles.init();
        ModSounds.init();
        ModCreativeTabs.init();
    }

    public static void commonSetup() {
        for (GrenadeKind kind : GrenadeKind.values()) {
            //? if >=1.21 {
            DispenserBlock.registerProjectileBehavior(ModItems.grenade(kind).get());
            //?} else {
            /*DispenserBlock.registerBehavior(ModItems.grenade(kind).get(), new AbstractProjectileDispenseBehavior() {
                @Override
                protected Projectile getProjectile(Level level, Position pos, ItemStack stack) {
                    return new GrenadeEntity(level, pos, stack);
                }

                @Override
                protected float getUncertainty() {
                    return 3.0F;
                }

                @Override
                protected float getPower() {
                    return 0.9F;
                }
            });
            *///?}
        }
    }

    public static ResourceLocation id(String path) {
        return location(MOD_ID, path);
    }

    public static ResourceLocation location(String namespace, String path) {
        //? if >=1.21 {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
        //?} else {
        /*return new ResourceLocation(namespace, path);
        *///?}
    }
}
