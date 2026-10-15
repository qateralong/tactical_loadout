package tacz_camo_armor;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import tacz_camo_armor.compat.tacz.TaczCompat;
import tacz_camo_armor.config.ClientConfig;
import tacz_camo_armor.config.ServerConfig;
import tacz_camo_armor.grenade.GrenadeKind;
import tacz_camo_armor.registry.ModArmorMaterials;
import tacz_camo_armor.registry.ModCreativeTabs;
import tacz_camo_armor.registry.ModDataComponents;
import tacz_camo_armor.registry.ModEntities;
import tacz_camo_armor.registry.ModItems;
import tacz_camo_armor.registry.ModParticles;
import tacz_camo_armor.registry.ModRecipes;
import tacz_camo_armor.registry.ModSounds;

@Mod(TaczCamoArmor.MOD_ID)
public class TaczCamoArmor {
    public static final String MOD_ID = "tacz_camo_armor";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TaczCamoArmor(IEventBus modBus, ModContainer container) {
        ModArmorMaterials.ARMOR_MATERIALS.register(modBus);
        ModDataComponents.COMPONENTS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModRecipes.SERIALIZERS.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);
        ModParticles.PARTICLE_TYPES.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        modBus.addListener(TaczCamoArmor::commonSetup);
        container.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        if (ModList.get().isLoaded("tacz")) {
            TaczCompat.init();
        }
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            for (GrenadeKind kind : GrenadeKind.values()) {
                DispenserBlock.registerProjectileBehavior(ModItems.grenade(kind).get());
            }
        });
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
