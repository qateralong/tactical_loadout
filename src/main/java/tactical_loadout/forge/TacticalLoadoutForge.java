package tactical_loadout.forge;

import fuzs.forgeconfigapiport.forge.api.neoforge.v4.NeoForgeConfigRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.config.ClientConfig;
import tactical_loadout.config.ServerConfig;
import tactical_loadout.platform.Platform;

@Mod(TacticalLoadout.MOD_ID)
public final class TacticalLoadoutForge {
    public TacticalLoadoutForge(FMLJavaModLoadingContext context) {
        TacticalLoadout.init();
        ((ForgePlatform) Platform.INSTANCE).registerAll(context.getModEventBus());
        ForgeNetwork.init();
        context.getModEventBus().addListener((FMLCommonSetupEvent event) -> event.enqueueWork(TacticalLoadout::commonSetup));
        NeoForgeConfigRegistry.INSTANCE.register(TacticalLoadout.MOD_ID, ModConfig.Type.SERVER, ServerConfig.SPEC);
        NeoForgeConfigRegistry.INSTANCE.register(TacticalLoadout.MOD_ID, ModConfig.Type.CLIENT, ClientConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(ForgeEvents.class);
    }
}
