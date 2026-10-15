package tactical_loadout.grenade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import tactical_loadout.registry.ModParticles;
//? if >=1.20.5 {
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
//?} else {
/*import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
*///?}

public record SmokePuffOptions(int lifetime) implements ParticleOptions {
    //? if >=1.20.5 {
    public static final MapCodec<SmokePuffOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("lifetime").forGetter(SmokePuffOptions::lifetime)
    ).apply(instance, SmokePuffOptions::new));
    public static final StreamCodec<ByteBuf, SmokePuffOptions> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(SmokePuffOptions::new, SmokePuffOptions::lifetime);
    //?} else {
    /*public static final Codec<SmokePuffOptions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("lifetime").forGetter(SmokePuffOptions::lifetime)
    ).apply(instance, SmokePuffOptions::new));
    @SuppressWarnings("deprecation")
    public static final Deserializer<SmokePuffOptions> DESERIALIZER = new Deserializer<>() {
        @Override
        public SmokePuffOptions fromCommand(ParticleType<SmokePuffOptions> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            return new SmokePuffOptions(reader.readInt());
        }

        @Override
        public SmokePuffOptions fromNetwork(ParticleType<SmokePuffOptions> type, FriendlyByteBuf buf) {
            return new SmokePuffOptions(buf.readVarInt());
        }
    };

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeVarInt(lifetime);
    }

    @Override
    public String writeToString() {
        return ModParticles.SMOKE_PUFF.id() + " " + lifetime;
    }
    *///?}

    @Override
    public ParticleType<SmokePuffOptions> getType() {
        return ModParticles.SMOKE_PUFF.get();
    }
}
