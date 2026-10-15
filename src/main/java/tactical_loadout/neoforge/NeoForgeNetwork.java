package tactical_loadout.neoforge;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.client.FlashOverlay;
import tactical_loadout.device.Devices;
import tactical_loadout.gear.Restraints;
import tactical_loadout.client.ClientHooks;
import tactical_loadout.network.DownedPayload;
import tactical_loadout.network.FlashPayload;
import tactical_loadout.network.RestraintPayload;
import tactical_loadout.network.StrugglePayload;
import tactical_loadout.network.ToggleNvgPayload;

@EventBusSubscriber(modid = TacticalLoadout.MOD_ID)
public final class NeoForgeNetwork {
    private NeoForgeNetwork() {
    }

    @SubscribeEvent
    static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToClient(FlashPayload.TYPE, FlashPayload.STREAM_CODEC,
                        (payload, context) -> context.enqueueWork(() -> FlashOverlay.trigger(payload.strength(), payload.ticks())))
                .playToServer(ToggleNvgPayload.TYPE, ToggleNvgPayload.STREAM_CODEC,
                        (payload, context) -> context.enqueueWork(() -> Devices.toggleNvg(context.player())))
                .playToClient(RestraintPayload.TYPE, RestraintPayload.STREAM_CODEC,
                        (payload, context) -> context.enqueueWork(() -> Restraints.setClientRemaining(payload.remaining())))
                .playToClient(DownedPayload.TYPE, DownedPayload.STREAM_CODEC,
                        (payload, context) -> context.enqueueWork(() -> ClientHooks.onDowned(payload.entityId(), payload.ticks())))
                .playToServer(StrugglePayload.TYPE, StrugglePayload.STREAM_CODEC,
                        (payload, context) -> context.enqueueWork(() -> {
                            if (context.player() instanceof ServerPlayer player) {
                                Restraints.struggle(player);
                            }
                        }));
    }
}
