package tactical_loadout.legacyforge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.client.ClientHooks;
import tactical_loadout.client.DownedOverlay;
import tactical_loadout.client.FlashOverlay;
import tactical_loadout.client.RestraintOverlay;
import tactical_loadout.gear.Helpless;
import tactical_loadout.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import tactical_loadout.client.GrenadeRenderer;
import tactical_loadout.client.SmokePuffParticle;
import tactical_loadout.registry.ModEntities;
import tactical_loadout.registry.ModParticles;

/** Forwards Forge client events to the shared client hooks; the night vision tint comes from GameRendererMixin. */
public final class LegacyForgeClientEvents {
    private LegacyForgeClientEvents() {
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
        public static void registerOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("downed", (gui, graphics, partialTick, width, height) -> DownedOverlay.render(graphics, partialTick));
            event.registerAboveAll("restraint", (gui, graphics, partialTick, width, height) -> RestraintOverlay.render(graphics, partialTick));
            event.registerAboveAll("flash", (gui, graphics, partialTick, width, height) -> FlashOverlay.render(graphics, partialTick));
        }

        @SubscribeEvent
        public static void setup(FMLClientSetupEvent event) {
            // The riot shield swaps to its raised model while blocking, like the vanilla shield.
            event.enqueueWork(() -> ItemProperties.register(ModItems.RIOT_SHIELD.get(), TacticalLoadout.location("minecraft", "blocking"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F));
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
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase == TickEvent.Phase.END && event.player.level().isClientSide) {
                ClientHooks.onPlayerTick(event.player);
            }
        }

        @SubscribeEvent
        public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
            Player player = Minecraft.getInstance().player;
            if (player != null && Helpless.is(player)) {
                event.setSwingHand(false);
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                ClientHooks.onClientTick();
            }
        }
    }
}
