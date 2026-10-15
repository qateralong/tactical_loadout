package tactical_loadout.legacyforge;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.config.ClientConfig;
import tactical_loadout.config.ServerConfig;
import tactical_loadout.platform.Platform;

@Mod(TacticalLoadout.MOD_ID)
public final class TacticalLoadoutLegacyForge {
    public TacticalLoadoutLegacyForge() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        TacticalLoadout.init();
        ((LegacyForgePlatform) Platform.INSTANCE).registerAll(modBus);
        LegacyForgeNetwork.init();
        modBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(TacticalLoadout::commonSetup));
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(LegacyForgeEvents.class);
    }
}
