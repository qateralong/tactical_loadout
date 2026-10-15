package tactical_loadout.registry;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
//? if >=1.20.5 {
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
//?} else {
/*import com.mojang.serialization.Codec;
*///?}
import tactical_loadout.platform.Platform;
import tactical_loadout.platform.RegistryEntry;
import tactical_loadout.grenade.SmokePuffOptions;

public final class ModParticles {
    public static final RegistryEntry<ParticleType<?>, ParticleType<SmokePuffOptions>> SMOKE_PUFF =
            //? if >=1.20.5 {
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
            //?} else {
            /*Platform.INSTANCE.register(Registries.PARTICLE_TYPE, "smoke_puff",
                    () -> new ParticleType<SmokePuffOptions>(true, SmokePuffOptions.DESERIALIZER) {
                        @Override
                        public Codec<SmokePuffOptions> codec() {
                            return SmokePuffOptions.CODEC;
                        }
                    });
            *///?}

    private ModParticles() {
    }

    /** Loads the class so its entries get registered. */
    public static void init() {
    }
}
