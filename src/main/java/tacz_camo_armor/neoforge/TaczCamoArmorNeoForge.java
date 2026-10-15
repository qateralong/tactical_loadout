package tacz_camo_armor.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.config.ClientConfig;
import tacz_camo_armor.config.ServerConfig;
import tacz_camo_armor.platform.Platform;

@Mod(TaczCamoArmor.MOD_ID)
public final class TaczCamoArmorNeoForge {
    public TaczCamoArmorNeoForge(IEventBus modBus, ModContainer container) {
        TaczCamoArmor.init();
        ((NeoForgePlatform) Platform.INSTANCE).registerAll(modBus);
        modBus.addListener(FMLCommonSetupEvent.class, event -> event.enqueueWork(TaczCamoArmor::commonSetup));
        container.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        if (Platform.INSTANCE.isModLoaded("tacz")) {
            TaczCompat.init();
        }
    }
}
