package tacz_camo_armor;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(TaczCamoArmor.MOD_ID)
public class TaczCamoArmor {
    public static final String MOD_ID = "tacz_camo_armor";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TaczCamoArmor(IEventBus modBus, ModContainer container) {
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
