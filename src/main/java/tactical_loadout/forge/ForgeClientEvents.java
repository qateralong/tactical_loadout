package tactical_loadout.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.gui.overlay.ForgeLayeredDraw;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.client.ClientHooks;
import tactical_loadout.client.FlashOverlay;
import tactical_loadout.client.GrenadeRenderer;
import tactical_loadout.client.NvgOverlay;
import tactical_loadout.client.SmokePuffParticle;
import tactical_loadout.registry.ModEntities;
import tactical_loadout.registry.ModParticles;

/** Forwards Forge client events to the shared client hooks. */
public final class ForgeClientEvents {
    private ForgeClientEvents() {
    }

    @Mod.EventBusSubscriber(modid = TacticalLoadout.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBus {
        private ModBus() {
        }

        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            ClientHooks.TOGGLE_NVG.setKeyConflictContext(KeyConflictContext.IN_GAME);
            event.register(ClientHooks.TOGGLE_NVG);
        }

        @SubscribeEvent
        public static void registerLayers(AddGuiOverlayLayersEvent event) {
            ForgeLayeredDraw draw = event.getLayeredDraw();
            // At the root, under the HUD stack, so F1 hides the HUD but not the view through the tubes.
            draw.add(TacticalLoadout.id("nvg"), NvgOverlay::render);
            draw.putBelow(ForgeLayeredDraw.VANILLA_ROOT, TacticalLoadout.id("nvg"), ForgeLayeredDraw.PRE_SLEEP_STACK);
            draw.add(TacticalLoadout.id("flash"), FlashOverlay::render);
        }

        @SubscribeEvent
        public static void registerParticles(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ModParticles.SMOKE_PUFF.get(), SmokePuffParticle.Provider::new);
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.GRENADE.get(), GrenadeRenderer::new);
        }
    }

    @Mod.EventBusSubscriber(modid = TacticalLoadout.MOD_ID, value = Dist.CLIENT)
    public static final class GameBus {
        private GameBus() {
        }

        @SubscribeEvent
        public static void onFov(ComputeFovModifierEvent event) {
            event.setNewFovModifier(ClientHooks.fovModifier(event.getPlayer(), event.getNewFovModifier()));
        }

        @SubscribeEvent
        public static void onMovementInput(MovementInputUpdateEvent event) {
            ClientHooks.onMovementInput(event.getEntity(), event.getInput());
        }

        @SubscribeEvent
        public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
            ClientHooks.onLoggingOut();
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent.Post event) {
            ClientHooks.onClientTick();
        }
    }
}
