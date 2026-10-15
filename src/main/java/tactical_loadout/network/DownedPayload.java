package tactical_loadout.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import tactical_loadout.TacticalLoadout;

/** Tells a player how many ticks they have left before bleeding out; 0 means they are not down. */
public record DownedPayload(int ticks) implements CustomPacketPayload {
    public static final Type<DownedPayload> TYPE = new Type<>(TacticalLoadout.id("downed"));
    public static final StreamCodec<ByteBuf, DownedPayload> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(DownedPayload::new, DownedPayload::ticks);

    @Override
    public Type<DownedPayload> type() {
        return TYPE;
    }
}
