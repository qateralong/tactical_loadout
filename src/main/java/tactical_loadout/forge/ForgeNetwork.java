package tactical_loadout.forge;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.client.FlashOverlay;
import tactical_loadout.device.Devices;
import tactical_loadout.client.ClientHooks;
import tactical_loadout.gear.Restraints;
import tactical_loadout.network.DownedPayload;
import tactical_loadout.network.FlashPayload;
import tactical_loadout.network.RestraintPayload;
import tactical_loadout.network.StrugglePayload;
import tactical_loadout.network.ToggleNvgPayload;

final class ForgeNetwork {
    static final Channel<CustomPacketPayload> CHANNEL = ChannelBuilder.named(TacticalLoadout.id("main"))
            .networkProtocolVersion(1)
            .payloadChannel()
            .play()
                .clientbound()
                    .addMain(FlashPayload.TYPE, codec(FlashPayload.STREAM_CODEC),
                            (payload, context) -> FlashOverlay.trigger(payload.strength(), payload.ticks()))
                    .addMain(RestraintPayload.TYPE, codec(RestraintPayload.STREAM_CODEC),
                            (payload, context) -> Restraints.setClientRemaining(payload.remaining()))
                    .addMain(DownedPayload.TYPE, codec(DownedPayload.STREAM_CODEC),
                            (payload, context) -> ClientHooks.onDowned(payload.entityId(), payload.ticks()))
                .serverbound()
                    .addMain(ToggleNvgPayload.TYPE, codec(ToggleNvgPayload.STREAM_CODEC),
                            (payload, context) -> Devices.toggleNvg(context.getSender()))
                    .addMain(StrugglePayload.TYPE, codec(StrugglePayload.STREAM_CODEC),
                            (payload, context) -> Restraints.struggle(context.getSender()))
            .build();

    private ForgeNetwork() {
    }

    static void init() {
    }

    // The payload codecs only read plain bytes, so they work on any buffer type the channel uses.
    @SuppressWarnings("unchecked")
    private static <B extends FriendlyByteBuf, T> StreamCodec<B, T> codec(StreamCodec<? super B, T> codec) {
        return (StreamCodec<B, T>) codec;
    }
}
