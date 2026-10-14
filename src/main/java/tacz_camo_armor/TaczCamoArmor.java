package tacz_camo_armor;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import tacz_camo_armor.config.ServerConfig;
import tacz_camo_armor.registry.ModArmorMaterials;
import tacz_camo_armor.registry.ModCreativeTabs;
import tacz_camo_armor.registry.ModDataComponents;
import tacz_camo_armor.registry.ModEntities;
import tacz_camo_armor.registry.ModItems;
import tacz_camo_armor.registry.ModRecipes;

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
        ModCreativeTabs.TABS.register(modBus);
        container.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
