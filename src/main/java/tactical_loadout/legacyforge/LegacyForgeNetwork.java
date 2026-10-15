package tactical_loadout.legacyforge;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.client.FlashOverlay;
import tactical_loadout.device.Devices;
import tactical_loadout.network.FlashPayload;
import tactical_loadout.network.ToggleNvgPayload;

import java.util.Optional;

final class LegacyForgeNetwork {
    private static final String VERSION = "1";
    static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(TacticalLoadout.id("main"),
            () -> VERSION, VERSION::equals, VERSION::equals);

    private LegacyForgeNetwork() {
    }

    static void init() {
        CHANNEL.registerMessage(0, FlashPayload.class, FlashPayload::write, FlashPayload::read, (payload, context) -> {
            context.get().enqueueWork(() -> FlashOverlay.trigger(payload.strength(), payload.ticks()));
            context.get().setPacketHandled(true);
        }, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(1, ToggleNvgPayload.class, ToggleNvgPayload::write, ToggleNvgPayload::read, (payload, context) -> {
            context.get().enqueueWork(() -> Devices.toggleNvg(context.get().getSender()));
            context.get().setPacketHandled(true);
        }, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }
}
