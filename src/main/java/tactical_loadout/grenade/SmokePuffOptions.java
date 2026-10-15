package tactical_loadout.grenade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import tactical_loadout.registry.ModParticles;

public record SmokePuffOptions(int lifetime) implements ParticleOptions {
    public static final MapCodec<SmokePuffOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("lifetime").forGetter(SmokePuffOptions::lifetime)
    ).apply(instance, SmokePuffOptions::new));
    public static final StreamCodec<ByteBuf, SmokePuffOptions> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(SmokePuffOptions::new, SmokePuffOptions::lifetime);

    @Override
    public ParticleType<SmokePuffOptions> getType() {
        return ModParticles.SMOKE_PUFF.get();
    }
}
