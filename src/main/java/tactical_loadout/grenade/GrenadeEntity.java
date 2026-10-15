package tactical_loadout.grenade;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import tactical_loadout.config.ServerConfig;
import tactical_loadout.registry.ModEntities;
import tactical_loadout.registry.ModItems;
import tactical_loadout.registry.ModSounds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

public class GrenadeEntity extends ThrowableItemProjectile {
    private static final int PUFFS = 120;
    private static final int FILL_TICKS = 40;
    private static final double RESEND_RANGE = 160.0;
    private static final double GOLDEN_ANGLE = Math.PI * (3 - Math.sqrt(5));
    // An impact grenade arms only after flying this far, so one dropped at the feet does not go off.
    private static final double IMPACT_ARM_DISTANCE = 3.5;
    private int fuse = GrenadeKind.FRAG.fuseTicks();
    // Ticks left of the lasting effect (smoke, fire or decoy shots); -1 while the grenade is still live.
    private int activeTicks = -1;
    private int activeTotal;
    // Identity, not UUID: a player who relogs gets a new ServerPlayer and an empty particle engine.
    private final Set<ServerPlayer> smokeViewers = Collections.newSetFromMap(new WeakHashMap<>());
    private IncendiaryFire fire;
    private int nextShot;
    private int burstLeft;
    private float spin;
    private float spinO;
    private float heading;
    private Vec3 origin;

    public GrenadeEntity(EntityType<? extends GrenadeEntity> type, Level level) {
        super(type, level);
    }

    public GrenadeEntity(Level level, LivingEntity thrower, ItemStack stack) {
        super(ModEntities.GRENADE.get(), thrower, level);
        setItem(stack.copyWithCount(1));
        fuse = kind().fuseTicks();
        origin = position();
    }

    public GrenadeEntity(Level level, Position pos, ItemStack stack) {
        super(ModEntities.GRENADE.get(), pos.x(), pos.y(), pos.z(), level);
        setItem(stack.copyWithCount(1));
        fuse = kind().fuseTicks();
        origin = position();
    }

    public GrenadeKind kind() {
        return getItem().getItem() instanceof GrenadeItem grenade ? grenade.kind() : GrenadeKind.FRAG;
    }

    public boolean isSmoking() {
        return kind() == GrenadeKind.SMOKE && activeTicks > 0;
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.grenade(GrenadeKind.FRAG).get();
    }

    // Death messages name the grenade ("blown up by Frag Grenade") when nobody threw it, e.g. from a dispenser.
    @Override
    protected Component getTypeName() {
        return getItem().getHoverName();
    }

    public float spin(float partialTick) {
        return Mth.lerp(partialTick, spinO, spin);
    }

    public float heading() {
        return heading;
    }

    @Override
    public void tick() {
        if (isNoGravity() && level().noCollision(this, getBoundingBox().move(0.0, -0.1, 0.0))) {
            setNoGravity(false);
        }
        super.tick();
        if (!(level() instanceof ServerLevel level)) {
            updateSpin();
            return;
        }
        if (activeTicks >= 0) {
            int elapsed = Math.max(0, activeTotal - activeTicks);
            boolean alive = switch (kind()) {
                case SMOKE -> {
                    tickSmoke(level, elapsed);
                    yield true;
                }
                case MOLOTOV, INCENDIARY -> tickFire(level, elapsed);
                case DECOY -> {
                    tickDecoy(level);
                    yield true;
                }
                default -> false;
            };
            if (!alive || --activeTicks <= 0) {
                if (alive && kind() == GrenadeKind.DECOY) {
                    level.explode(this, getX(), getY(0.5), getZ(), 1.0F, Level.ExplosionInteraction.NONE);
                }
                discard();
            }
            return;
        }
        if (--fuse <= 0) {
            detonate(level);
        }
    }

    // Tumbles while flying, then settles on its side so a resting grenade lies flat.
    private void updateSpin() {
        spinO = spin;
        Vec3 velocity = getDeltaMovement();
        if (velocity.horizontalDistanceSqr() > 1.0E-4) {
            heading = (float) Math.toDegrees(Mth.atan2(velocity.x, velocity.z));
        }
        if (velocity.lengthSqr() > 0.003) {
            spin += (float) velocity.length() * 45.0F;
        } else {
            float rest = Math.round((spin - 90.0F) / 180.0F) * 180.0F + 90.0F;
            spin += (rest - spin) * 0.35F;
        }
    }

    private void tickSmoke(ServerLevel level, int elapsed) {
        if (elapsed < FILL_TICKS) {
            List<ServerPlayer> nearby = nearbyPlayers(level);
            if (elapsed == 0) {
                smokeViewers.addAll(nearby);
            }
            nearby.removeIf(player -> !smokeViewers.contains(player));
            for (int i = 0; i < PUFFS / FILL_TICKS; i++) {
                emitPuff(level, nearby, elapsed * (PUFFS / FILL_TICKS) + i, activeTicks + random.nextInt(40));
            }
        } else if (tickCount % 10 == 0) {
            resendCloud(level);
        }
    }

    private boolean tickFire(ServerLevel level, int elapsed) {
        setInvisible(true);
        if (fire == null) {
            fire = IncendiaryFire.spread(level, position(), ServerConfig.fireRadius());
            if (fire == null) {
                IncendiaryFire.fizzle(level, position());
                return false;
            }
        }
        if (!fire.tick(level, this, elapsed, activeTicks)) {
            IncendiaryFire.fizzle(level, position());
            return false;
        }
        return true;
    }

    // Fake gunfire in short bursts; every burst draws idle monsters over and sets off sculk and wardens.
    private void tickDecoy(ServerLevel level) {
        if (--nextShot > 0) {
            return;
        }
        level.playSound(null, getX(), getY(), getZ(), ModSounds.DECOY_SHOT.get(), SoundSource.NEUTRAL, 4.0F,
                0.9F + random.nextFloat() * 0.15F);
        level.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 0.2, getZ(), 2, 0.05, 0.05, 0.05, 0.01);
        level.gameEvent(this, GameEvent.PROJECTILE_SHOOT, position());
        if (burstLeft-- > 0) {
            nextShot = 3 + random.nextInt(2);
            return;
        }
        burstLeft = random.nextInt(5);
        nextShot = 15 + random.nextInt(30);
        double radius = ServerConfig.decoyLureRadius();
        if (radius <= 0) {
            return;
        }
        for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(position(), position()).inflate(radius))) {
            if (mob instanceof Enemy && mob.getTarget() == null && !Flashbang.isBlinded(mob)) {
                mob.getNavigation().moveTo(getX(), getY(), getZ(), 1.0);
            }
        }
    }

    // Players who arrive late, or after the world was reloaded, get the whole cloud at once.
    private void resendCloud(ServerLevel level) {
        List<ServerPlayer> late = nearbyPlayers(level);
        late.removeIf(player -> !smokeViewers.add(player));
        if (!late.isEmpty()) {
            for (int i = 0; i < PUFFS; i++) {
                emitPuff(level, late, i, activeTicks + random.nextInt(40));
            }
        }
    }

    private List<ServerPlayer> nearbyPlayers(ServerLevel level) {
        List<ServerPlayer> players = new ArrayList<>();
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(this) < RESEND_RANGE * RESEND_RANGE) {
                players.add(player);
            }
        }
        return players;
    }

    // Fills a dome evenly from the centre outwards; each puff lives until the smoke ends, so the cloud never thins out.
    private void emitPuff(ServerLevel level, List<ServerPlayer> players, int index, int lifetime) {
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
        for (ServerPlayer player : players) {
            level.sendParticles(player, puff, true, x, y, z, 1, 0, 0, 0, 0.003);
        }
    }

    private void activate(int ticks) {
        activeTicks = ticks;
        activeTotal = ticks;
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
            case IMPACT -> {
                level.explode(this, getX(), getY(0.5), getZ(), (float) ServerConfig.impactRadius(), Level.ExplosionInteraction.TNT);
                discard();
            }
            case FLASH -> {
                Flashbang.detonate(level, this);
                discard();
            }
            case SMOKE -> {
                activate(ServerConfig.smokeSeconds() * 20);
                level.playSound(null, getX(), getY(), getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 1.2F, 0.6F);
                SmokeClouds.add(this);
            }
            case MOLOTOV, INCENDIARY -> ignite(level);
            case DECOY -> {
                activate(ServerConfig.decoySeconds() * 20);
                nextShot = 5;
                burstLeft = random.nextInt(4);
            }
        }
    }

    // Fire grenades burst on the first floor they land on, or wherever they are when the fuse runs out.
    private void ignite(ServerLevel level) {
        setDeltaMovement(Vec3.ZERO);
        setNoGravity(true);
        setInvisible(true);
        IncendiaryFire.ignite(level, this);
        activate(ServerConfig.fireSeconds() * 20);
    }

    private boolean impactArmed() {
        return kind() == GrenadeKind.IMPACT && origin != null && position().distanceTo(origin) >= IMPACT_ARM_DISTANCE;
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        if (activeTicks >= 0 && kind().isFire()) {
            return;
        }
        if (impactArmed() && level() instanceof ServerLevel level) {
            setPos(hit.getLocation());
            detonate(level);
            return;
        }
        setNoGravity(false);
        Vec3 velocity = getDeltaMovement();
        Direction.Axis axis = hit.getDirection().getAxis();
        double bounce = 0.35;
        double friction = 0.6;
        velocity = switch (axis) {
            case X -> new Vec3(-velocity.x * bounce, velocity.y * friction, velocity.z * friction);
            case Y -> new Vec3(velocity.x * friction, -velocity.y * bounce, velocity.z * friction);
            case Z -> new Vec3(velocity.x * friction, velocity.y * friction, -velocity.z * bounce);
        };
        setPos(hit.getLocation().add(Vec3.atLowerCornerOf(hit.getDirection().getNormal()).scale(0.05)));
        if (kind().isFire() && hit.getDirection() == Direction.UP && level() instanceof ServerLevel level) {
            ignite(level);
            return;
        }
        if (velocity.lengthSqr() > 0.01) {
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.CHAIN_HIT, SoundSource.NEUTRAL, 0.5F, 1.6F);
        } else if (hit.getDirection() == Direction.UP) {
            // Resting on the ground: stop falling so the grenade does not hit the block again every tick
            // (that would ring bells, power targets and trigger sculk sensors until it goes off).
            velocity = Vec3.ZERO;
            setNoGravity(true);
        }
        setDeltaMovement(velocity);
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        if (impactArmed() && level() instanceof ServerLevel level) {
            detonate(level);
            return;
        }
        setDeltaMovement(getDeltaMovement().scale(-0.2));
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide) {
            SmokeClouds.remove(this);
        }
        super.remove(reason);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Fuse", fuse);
        tag.putInt("ActiveTicks", activeTicks);
        tag.putInt("ActiveTotal", activeTotal);
        if (origin != null) {
            tag.putDouble("OriginX", origin.x);
            tag.putDouble("OriginY", origin.y);
            tag.putDouble("OriginZ", origin.z);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        fuse = tag.contains("Fuse") ? tag.getInt("Fuse") : kind().fuseTicks();
        String key = tag.contains("ActiveTicks") ? "ActiveTicks" : "SmokeTicks";
        activeTicks = tag.contains(key) ? tag.getInt(key) : -1;
        activeTotal = tag.contains("ActiveTotal") ? tag.getInt("ActiveTotal") : Math.max(activeTicks, ServerConfig.smokeSeconds() * 20);
        if (tag.contains("OriginX")) {
            origin = new Vec3(tag.getDouble("OriginX"), tag.getDouble("OriginY"), tag.getDouble("OriginZ"));
        }
        if (isSmoking() && !level().isClientSide) {
            SmokeClouds.add(this);
        }
    }
}
