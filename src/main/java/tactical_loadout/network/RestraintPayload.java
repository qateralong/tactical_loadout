package tactical_loadout.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import tactical_loadout.TacticalLoadout;

/** Tells a player how many struggles they still need to break free; 0 means they are not tied. */
public record RestraintPayload(int remaining) implements CustomPacketPayload {
    public static final Type<RestraintPayload> TYPE = new Type<>(TacticalLoadout.id("restraint"));
    public static final StreamCodec<ByteBuf, RestraintPayload> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(RestraintPayload::new, RestraintPayload::remaining);

    @Override
    public Type<RestraintPayload> type() {
        return TYPE;
    }
}
