package tactical_loadout.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import tactical_loadout.client.RestraintOverlay;
import tactical_loadout.gear.Helpless;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.client.ClientHooks;
import tactical_loadout.client.FlashOverlay;
import tactical_loadout.client.GrenadeRenderer;
import tactical_loadout.client.NvgOverlay;
import tactical_loadout.client.SmokePuffParticle;
import tactical_loadout.registry.ModEntities;
import tactical_loadout.registry.ModParticles;

/** Forwards NeoForge client events to the shared client hooks. */
@EventBusSubscriber(modid = TacticalLoadout.MOD_ID, value = Dist.CLIENT)
public final class NeoForgeClientEvents {
    private NeoForgeClientEvents() {
    }

    @SubscribeEvent
    static void registerKeys(RegisterKeyMappingsEvent event) {
        ClientHooks.TOGGLE_NVG.setKeyConflictContext(KeyConflictContext.IN_GAME);
        event.register(ClientHooks.TOGGLE_NVG);
    }

    @SubscribeEvent
    static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CAMERA_OVERLAYS, TacticalLoadout.id("nvg"), NvgOverlay::render);
        event.registerAbove(VanillaGuiLayers.HOTBAR, TacticalLoadout.id("restraint"), RestraintOverlay::render);
        event.registerAboveAll(TacticalLoadout.id("flash"), FlashOverlay::render);
    }

    @SubscribeEvent
    static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.SMOKE_PUFF.get(), SmokePuffParticle.Provider::new);
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.GRENADE.get(), GrenadeRenderer::new);
    }

    @SubscribeEvent
    static void onFov(ComputeFovModifierEvent event) {
        event.setNewFovModifier(ClientHooks.fovModifier(event.getPlayer(), event.getNewFovModifier()));
    }

    @SubscribeEvent
    static void onMovementInput(MovementInputUpdateEvent event) {
        ClientHooks.onMovementInput(event.getEntity(), event.getInput());
    }

    @SubscribeEvent
    static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientHooks.onLoggingOut();
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        ClientHooks.onClientTick();
    }

    @SubscribeEvent
    static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        Player player = Minecraft.getInstance().player;
        if (player != null && Helpless.is(player)) {
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }
}
