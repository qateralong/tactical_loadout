package tactical_loadout.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import tactical_loadout.TacticalLoadout;

/** A player's downed state for everyone nearby: ticks left before bleeding out, 0 when they are not down. */
public record DownedPayload(int entityId, int ticks) implements CustomPacketPayload {
    public static final Type<DownedPayload> TYPE = new Type<>(TacticalLoadout.id("downed"));
    public static final StreamCodec<ByteBuf, DownedPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, DownedPayload::entityId,
            ByteBufCodecs.VAR_INT, DownedPayload::ticks,
            DownedPayload::new);

    @Override
    public Type<DownedPayload> type() {
        return TYPE;
    }
}
