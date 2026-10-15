package tactical_loadout.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import tactical_loadout.TacticalLoadout;

/** One press of the struggle key from a tied player. */
public record StrugglePayload() implements CustomPacketPayload {
    public static final Type<StrugglePayload> TYPE = new Type<>(TacticalLoadout.id("struggle"));
    public static final StreamCodec<ByteBuf, StrugglePayload> STREAM_CODEC = StreamCodec.unit(new StrugglePayload());

    @Override
    public Type<StrugglePayload> type() {
        return TYPE;
    }
}
