package tacz_camo_armor.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import tacz_camo_armor.platform.Platform;
import tacz_camo_armor.platform.RegistryEntry;
import tacz_camo_armor.grenade.SmokePuffOptions;

public final class ModParticles {
    public static final RegistryEntry<ParticleType<?>, ParticleType<SmokePuffOptions>> SMOKE_PUFF =
            Platform.INSTANCE.register(Registries.PARTICLE_TYPE, "smoke_puff", () -> new ParticleType<SmokePuffOptions>(true) {
                @Override
                public MapCodec<SmokePuffOptions> codec() {
                    return SmokePuffOptions.CODEC;
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, SmokePuffOptions> streamCodec() {
                    return SmokePuffOptions.STREAM_CODEC;
                }
            });

    private ModParticles() {
    }

    /** Loads the class so its entries get registered. */
    public static void init() {
    }
}
