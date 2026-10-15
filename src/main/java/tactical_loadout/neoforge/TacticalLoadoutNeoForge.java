package tactical_loadout.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.config.ClientConfig;
import tactical_loadout.config.ServerConfig;
import tactical_loadout.platform.Platform;

@Mod(TacticalLoadout.MOD_ID)
public final class TacticalLoadoutNeoForge {
    public TacticalLoadoutNeoForge(IEventBus modBus, ModContainer container) {
        TacticalLoadout.init();
        ((NeoForgePlatform) Platform.INSTANCE).registerAll(modBus);
        modBus.addListener(FMLCommonSetupEvent.class, event -> event.enqueueWork(TacticalLoadout::commonSetup));
        container.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        if (Platform.INSTANCE.isModLoaded("tacz")) {
            TaczCompat.init();
        }
    }
}
