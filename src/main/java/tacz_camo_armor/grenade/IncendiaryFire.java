package tacz_camo_armor.grenade;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.advancement.ModAdvancements;
import tacz_camo_armor.config.ServerConfig;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** The burning patch left by a molotov or incendiary grenade. */
public final class IncendiaryFire {
    public static final ResourceKey<DamageType> DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, TaczCamoArmor.id("incendiary"));
    private static final int SPREAD_TICKS = 12;
    private static final int FLOOR_SEARCH = 3;
    private static final double LOW_BLOCK = 0.5;

    private final List<Cell> cells;
    private final AABB bounds;

    record Cell(BlockPos pos, double floor) {
        AABB box() {
            return new AABB(pos.getX(), floor, pos.getZ(), pos.getX() + 1, floor + 1.0, pos.getZ() + 1);
        }

        Vec3 center() {
            return new Vec3(pos.getX() + 0.5, floor, pos.getZ() + 0.5);
        }
    }

    private IncendiaryFire(List<Cell> cells) {
        this.cells = cells;
        AABB box = cells.get(0).box();
        for (Cell cell : cells) {
            box = box.minmax(cell.box());
        }
        this.bounds = box;
    }

    /** Spreads like a liquid from the landing spot: along the floor, up and down single steps, never through walls or water. */
    static IncendiaryFire spread(Level level, Vec3 origin, double radius) {
        BlockPos start = findStart(level, BlockPos.containing(origin));
        if (start == null) {
            return null;
        }
        List<Cell> cells = new ArrayList<>();
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        seen.add(start);
        double limit = radius * radius;
        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            cells.add(new Cell(pos, floor(level, pos)));
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos side = pos.relative(direction);
                double dx = side.getX() + 0.5 - origin.x;
                double dz = side.getZ() + 0.5 - origin.z;
                if (dx * dx + dz * dz > limit) {
                    continue;
                }
                BlockPos next = step(level, pos, side);
                if (next != null && seen.add(next)) {
                    queue.add(next);
                }
            }
        }
        return cells.isEmpty() ? null : new IncendiaryFire(cells);
    }

    private static BlockPos findStart(Level level, BlockPos pos) {
        for (int i = 0; i <= FLOOR_SEARCH; i++) {
            BlockPos below = pos.below(i);
            if (canBurn(level, below)) {
                return below;
            }
            if (!isOpen(level, below)) {
                return null;
            }
        }
        return null;
    }

    private static BlockPos step(Level level, BlockPos from, BlockPos side) {
        if (canBurn(level, side)) {
            return side;
        }
        BlockPos up = side.above();
        if (!isOpen(level, side) && isOpen(level, from.above()) && canBurn(level, up)) {
            return up;
        }
        BlockPos down = side.below();
        if (isOpen(level, side) && canBurn(level, down)) {
            return down;
        }
        return null;
    }

    private static boolean isOpen(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        VoxelShape shape = state.getCollisionShape(level, pos);
        return state.getFluidState().isEmpty() && (shape.isEmpty() || shape.max(Direction.Axis.Y) <= LOW_BLOCK);
    }

    // A cell burns when it is open and stands on something solid: a full block, a slab, a carpet or a snow layer.
    private static boolean canBurn(Level level, BlockPos pos) {
        if (!isOpen(level, pos)) {
            return false;
        }
        if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
            return true;
        }
        BlockPos below = pos.below();
        return !level.getBlockState(below).getCollisionShape(level, below).isEmpty() && level.getFluidState(below).isEmpty();
    }

    private static double floor(Level level, BlockPos pos) {
        VoxelShape own = level.getBlockState(pos).getCollisionShape(level, pos);
        if (!own.isEmpty()) {
            return pos.getY() + own.max(Direction.Axis.Y);
        }
        BlockPos below = pos.below();
        return below.getY() + level.getBlockState(below).getCollisionShape(level, below).max(Direction.Axis.Y);
    }

    static void ignite(ServerLevel level, GrenadeEntity grenade) {
        Vec3 pos = grenade.position();
        if (grenade.kind() == GrenadeKind.MOLOTOV) {
            level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 1.2F, 0.9F);
        }
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.FIRECHARGE_USE, SoundSource.NEUTRAL, 1.5F, 0.7F);
    }

    static void fizzle(ServerLevel level, Vec3 pos) {
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 1.0F, 1.2F);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, pos.x, pos.y + 0.3, pos.z, 12, 0.4, 0.2, 0.4, 0.02);
    }

    /** Burns for one tick; returns false once the whole patch has been put out by smoke. */
    boolean tick(ServerLevel level, GrenadeEntity grenade, int elapsed, int remaining) {
        RandomSource random = level.getRandom();
        if (elapsed % 5 == 0 && !extinguishBySmoke(level)) {
            return false;
        }
        int lit = Math.min(cells.size(), (elapsed + 1) * cells.size() / SPREAD_TICKS + 1);
        float intensity = remaining < 20 ? remaining / 20.0F : 1.0F;
        for (int i = 0; i < lit; i++) {
            Cell cell = cells.get(i);
            if (random.nextFloat() < 0.5F * intensity) {
                double x = cell.pos().getX() + random.nextDouble();
                double z = cell.pos().getZ() + random.nextDouble();
                level.sendParticles(ParticleTypes.FLAME, x, cell.floor() + 0.1, z, 1, 0.15, 0.15, 0.15, 0.015);
            }
        }
        if (elapsed % 3 == 0) {
            Cell cell = cells.get(random.nextInt(lit));
            Vec3 c = cell.center();
            level.sendParticles(ParticleTypes.LARGE_SMOKE, c.x, c.y + 0.8, c.z, 1, 0.3, 0.2, 0.3, 0.01);
        }
        if (elapsed % 15 == 0) {
            Vec3 c = cells.get(0).center();
            level.playSound(null, c.x, c.y, c.z, SoundEvents.FIRE_AMBIENT, SoundSource.NEUTRAL, 1.5F, 0.8F + random.nextFloat() * 0.3F);
        }
        burn(level, grenade, lit);
        return true;
    }

    private void burn(ServerLevel level, GrenadeEntity grenade, int lit) {
        DamageSource source = new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DAMAGE),
                grenade, grenade.getOwner());
        float damage = (float) ServerConfig.fireDamage();
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, bounds)) {
            if (entity.isSpectator()) {
                continue;
            }
            AABB box = entity.getBoundingBox();
            for (int i = 0; i < lit; i++) {
                if (cells.get(i).box().intersects(box)) {
                    if (entity.hurt(source, damage) || entity.isOnFire()) {
                        entity.igniteForTicks(20);
                    }
                    break;
                }
            }
        }
    }

    // CS2 rule: smoke puts out every burning cell it covers.
    private boolean extinguishBySmoke(ServerLevel level) {
        GrenadeEntity smoker = null;
        for (int i = cells.size() - 1; i >= 0; i--) {
            GrenadeEntity smoke = SmokeClouds.coveringSmoke(level, cells.get(i).center());
            if (smoke != null) {
                smoker = smoke;
                cells.remove(i);
            }
        }
        if (smoker == null) {
            return true;
        }
        Vec3 c = smoker.position();
        level.playSound(null, c.x, c.y, c.z, SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 1.5F, 0.8F);
        if (cells.isEmpty()) {
            ModAdvancements.award(smoker.getOwner(), ModAdvancements.EXTINGUISH);
            return false;
        }
        return true;
    }
}
