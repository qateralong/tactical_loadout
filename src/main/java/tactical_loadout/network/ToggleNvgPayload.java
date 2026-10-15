package tactical_loadout.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import tactical_loadout.TacticalLoadout;

public record ToggleNvgPayload() implements CustomPacketPayload {
    public static final Type<ToggleNvgPayload> TYPE = new Type<>(TacticalLoadout.id("toggle_nvg"));
    public static final StreamCodec<ByteBuf, ToggleNvgPayload> STREAM_CODEC = StreamCodec.unit(new ToggleNvgPayload());

    @Override
    public Type<ToggleNvgPayload> type() {
        return TYPE;
    }
}
