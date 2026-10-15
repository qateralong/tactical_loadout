package tactical_loadout.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import tactical_loadout.TacticalLoadout;

public record FlashPayload(float strength, int ticks) implements CustomPacketPayload {
    public static final Type<FlashPayload> TYPE = new Type<>(TacticalLoadout.id("flash"));
    public static final StreamCodec<ByteBuf, FlashPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, FlashPayload::strength,
            ByteBufCodecs.VAR_INT, FlashPayload::ticks,
            FlashPayload::new);

    @Override
    public Type<FlashPayload> type() {
        return TYPE;
    }
}
