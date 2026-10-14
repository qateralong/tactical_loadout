package tacz_camo_armor.grenade;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import tacz_camo_armor.config.ServerConfig;
import tacz_camo_armor.registry.ModEntities;
import tacz_camo_armor.registry.ModItems;

public class GrenadeEntity extends ThrowableItemProjectile {
    private int fuse = GrenadeKind.FRAG.fuseTicks();
    private int smokeTicks = -1;

    public GrenadeEntity(EntityType<? extends GrenadeEntity> type, Level level) {
        super(type, level);
    }

    public GrenadeEntity(Level level, LivingEntity thrower, ItemStack stack) {
        super(ModEntities.GRENADE.get(), thrower, level);
        setItem(stack.copyWithCount(1));
        fuse = kind().fuseTicks();
    }

    public GrenadeKind kind() {
        return getItem().getItem() instanceof GrenadeItem grenade ? grenade.kind() : GrenadeKind.FRAG;
    }

    public boolean isSmoking() {
        return smokeTicks > 0;
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.grenade(GrenadeKind.FRAG).get();
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel level)) {
            return;
        }
        if (smokeTicks >= 0) {
            if (tickCount % 2 == 0) {
                double radius = ServerConfig.smokeRadius() * 0.45;
                level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX(), getY() + 0.8, getZ(), 24, radius, 0.7, radius, 0.006);
            }
            if (--smokeTicks <= 0) {
                discard();
            }
            return;
        }
        if (--fuse <= 0) {
            detonate(level);
        }
    }

    private void detonate(ServerLevel level) {
        switch (kind()) {
            case FRAG -> {
                level.explode(this, getX(), getY(0.5), getZ(), (float) ServerConfig.fragRadius(), Level.ExplosionInteraction.NONE);
                discard();
            }
            case HE -> {
                level.explode(this, getX(), getY(0.5), getZ(), (float) ServerConfig.heRadius(), Level.ExplosionInteraction.TNT);
                discard();
            }
            case FLASH -> {
                Flashbang.detonate(level, this);
                discard();
            }
            case SMOKE -> {
                smokeTicks = ServerConfig.smokeSeconds() * 20;
                level.playSound(null, getX(), getY(), getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 1.2F, 0.6F);
                SmokeClouds.add(this);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        Vec3 velocity = getDeltaMovement();
        Direction.Axis axis = hit.getDirection().getAxis();
        double bounce = 0.35;
        double friction = 0.6;
        velocity = switch (axis) {
            case X -> new Vec3(-velocity.x * bounce, velocity.y * friction, velocity.z * friction);
            case Y -> new Vec3(velocity.x * friction, -velocity.y * bounce, velocity.z * friction);
            case Z -> new Vec3(velocity.x * friction, velocity.y * friction, -velocity.z * bounce);
        };
        if (velocity.lengthSqr() > 0.01) {
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.CHAIN_HIT, SoundSource.NEUTRAL, 0.5F, 1.6F);
        } else if (axis == Direction.Axis.Y) {
            velocity = Vec3.ZERO;
        }
        setDeltaMovement(velocity);
        setPos(hit.getLocation().add(Vec3.atLowerCornerOf(hit.getDirection().getNormal()).scale(0.05)));
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        setDeltaMovement(getDeltaMovement().scale(-0.2));
    }

    @Override
    public void remove(RemovalReason reason) {
        SmokeClouds.remove(this);
        super.remove(reason);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Fuse", fuse);
        tag.putInt("SmokeTicks", smokeTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        fuse = tag.getInt("Fuse");
        smokeTicks = tag.contains("SmokeTicks") ? tag.getInt("SmokeTicks") : -1;
        if (smokeTicks > 0) {
            SmokeClouds.add(this);
        }
    }
}
