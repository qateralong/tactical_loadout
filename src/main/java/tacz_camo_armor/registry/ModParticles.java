package tacz_camo_armor.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.grenade.SmokePuffOptions;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, TaczCamoArmor.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, ParticleType<SmokePuffOptions>> SMOKE_PUFF =
            PARTICLE_TYPES.register("smoke_puff", () -> new ParticleType<SmokePuffOptions>(true) {
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
}
