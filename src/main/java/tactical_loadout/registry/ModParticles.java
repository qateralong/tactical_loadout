package tactical_loadout.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import tactical_loadout.platform.Platform;
import tactical_loadout.platform.RegistryEntry;
import tactical_loadout.grenade.SmokePuffOptions;

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
