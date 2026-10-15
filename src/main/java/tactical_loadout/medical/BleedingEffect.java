package tactical_loadout.medical;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;

/** Loses a little health every few seconds, ignoring armor, until it runs out or is stopped with a tourniquet or medkit. */
public class BleedingEffect extends MobEffect {
    private static final int INTERVAL = 60;

    public BleedingEffect() {
        super(MobEffectCategory.HARMFUL, 0x8A0303);
    }

    //? if >=1.20.5 {
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % Math.max(10, INTERVAL >> amplifier) == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        bleed(entity);
        return true;
    }
    //?} else {
    /*@Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % Math.max(10, INTERVAL >> amplifier) == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        bleed(entity);
    }
    *///?}

    private static void bleed(LivingEntity entity) {
        if (entity.level() instanceof ServerLevel level) {
            entity.hurt(Medical.bleedingSource(level), 1.0F);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.defaultBlockState()),
                    entity.getX(), entity.getY(0.5), entity.getZ(), 6, 0.2, 0.3, 0.2, 0.05);
        }
    }
}
