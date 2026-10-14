package tacz_camo_armor.grenade;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
    private static final int PUFFS = 120;
    private static final int FILL_TICKS = 40;
    private static final double GOLDEN_ANGLE = Math.PI * (3 - Math.sqrt(5));
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
            int elapsed = ServerConfig.smokeSeconds() * 20 - smokeTicks;
            if (elapsed < FILL_TICKS) {
                for (int i = 0; i < PUFFS / FILL_TICKS; i++) {
                    emitPuff(level, elapsed * (PUFFS / FILL_TICKS) + i, smokeTicks + random.nextInt(40));
                }
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

    // Fills a dome evenly from the centre outwards; each puff lives until the smoke ends, so the cloud never thins out.
    private void emitPuff(ServerLevel level, int index, int lifetime) {
        double radius = ServerConfig.smokeRadius() * 0.85;
        double fraction = (index + 0.5) / PUFFS;
        double distance = radius * Math.cbrt(fraction);
        double polar = Math.acos(1 - (index * 0.7548776662) % 1.0);
        double azimuth = index * GOLDEN_ANGLE;
        double horizontal = Math.sin(polar) * distance;
        double x = getX() + Math.cos(azimuth) * horizontal + (random.nextDouble() - 0.5) * 0.6;
        double z = getZ() + Math.sin(azimuth) * horizontal + (random.nextDouble() - 0.5) * 0.6;
        double y = getY() + 0.5 + Math.cos(polar) * distance * 0.7;
        SmokePuffOptions puff = new SmokePuffOptions(lifetime);
        for (ServerPlayer player : level.players()) {
            level.sendParticles(player, puff, true, x, y, z, 1, 0, 0, 0, 0.003);
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
