package tactical_loadout.combat;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tactical_loadout.advancement.ModAdvancements;
import tactical_loadout.config.ServerConfig;
import tactical_loadout.item.TacticalArmorItem;
import tactical_loadout.registry.ModTags;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/**
 * Zone-based protection against bullets and other projectiles.
 * <p>
 * Without a gun mod integration, every damage type in {@code #tactical_loadout:ballistic} (all projectiles by default)
 * is reduced by the armor piece covering the hit zone, worked out from the projectile's path or, for hitscan weapons,
 * from the shooter's line of sight. Integrations that know the exact hit call {@link #expect} right before the damage
 * is dealt and {@link #claim} their damage types so the generic path leaves them alone.
 */
public final class BallisticHits {
    private static final List<Predicate<DamageSource>> CLAIMS = new CopyOnWriteArrayList<>();
    private static final double HITSCAN_RANGE = 256.0;
    // Armor only wears once the hit gets past shields and invulnerability frames, which vanilla checks after the
    // incoming damage event, so the wear waits here until the damage is actually dealt.
    private static final Map<LivingEntity, Wear> PENDING_WEAR = new WeakHashMap<>();
    // Set by an integration and consumed by the hurt call that follows it on the same thread.
    private static ExpectedHit expected;

    private record ExpectedHit(LivingEntity target, DamageSource source, EquipmentSlot slot, float damage) {
    }

    private record Wear(DamageSource source, EquipmentSlot slot, float damage) {
    }

    private BallisticHits() {
    }

    /** Marks damage that an integration handles itself through {@link #expect}. */
    public static void claim(Predicate<DamageSource> handled) {
        CLAIMS.add(handled);
    }

    /**
     * Announces that {@code source} is about to hurt {@code target} on {@code slot}: the next hurt call with this exact
     * source is reduced by the piece's protection, and the piece wears by {@code damage} if the hit goes through.
     */
    public static void expect(LivingEntity target, DamageSource source, EquipmentSlot slot, float damage) {
        expected = target.getItemBySlot(slot).getItem() instanceof TacticalArmorItem
                ? new ExpectedHit(target, source, slot, damage) : null;
    }

    public static void cancelExpected() {
        expected = null;
    }

    public static boolean isClaimed(DamageSource source) {
        for (Predicate<DamageSource> claim : CLAIMS) {
            if (claim.test(source)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBallistic(DamageSource source) {
        return source.is(ModTags.BALLISTIC) || isClaimed(source);
    }

    public static EquipmentSlot zoneSlot(AABB box, double hitY) {
        double height = (hitY - box.minY) / box.getYsize();
        return height >= ServerConfig.headZoneBottom() ? EquipmentSlot.HEAD : bodyZoneSlot(height);
    }

    public static EquipmentSlot bodyZoneSlot(double height) {
        if (height < ServerConfig.bootsZoneTop()) {
            return EquipmentSlot.FEET;
        }
        return height < ServerConfig.legsZoneTop() ? EquipmentSlot.LEGS : EquipmentSlot.CHEST;
    }

    public static double hitY(AABB box, Vec3 start, Vec3 end) {
        return box.clip(start, end).map(hit -> hit.y).orElse(Mth.clamp(start.y, box.minY, box.maxY));
    }

    private static void wear(LivingEntity target, EquipmentSlot slot, float damage) {
        ItemStack stack = target.getItemBySlot(slot);
        if (stack.getItem() instanceof TacticalArmorItem) {
            int wear = Math.max(1, Math.round(damage * (float) ServerConfig.durabilityPerDamage()));
            //? if >=1.21 {
            stack.hurtAndBreak(wear, target, slot);
            //?} else {
            /*stack.hurtAndBreak(wear, target, entity -> entity.broadcastBreakEvent(slot));
            *///?}
        }
    }

    private static float reduction(LivingEntity target, EquipmentSlot slot) {
        return target.getItemBySlot(slot).getItem() instanceof TacticalArmorItem armor
                ? (float) ServerConfig.bulletReduction(armor.tier()) : 0.0F;
    }

    private static EquipmentSlot guessSlot(LivingEntity target, DamageSource source) {
        AABB box = target.getBoundingBox();
        Entity direct = source.getDirectEntity();
        if (direct instanceof LivingEntity shooter) {
            Vec3 eye = shooter.getEyePosition();
            return zoneSlot(box, hitY(box, eye, eye.add(shooter.getLookAngle().scale(HITSCAN_RANGE))));
        }
        if (direct != null) {
            Vec3 start = direct.position();
            return zoneSlot(box, hitY(box, start, start.add(direct.getDeltaMovement())));
        }
        Vec3 from = source.getSourcePosition();
        return from == null ? EquipmentSlot.CHEST : zoneSlot(box, Mth.clamp(from.y, box.minY, box.maxY));
    }

    /** Called when damage is about to be dealt; returns the damage left after the zone protection. */
    public static float onIncomingDamage(LivingEntity target, DamageSource source, float amount) {
        if (target.level().isClientSide) {
            return amount;
        }
        ExpectedHit hit = expected;
        if (hit != null && hit.source() == source && hit.target() == target) {
            expected = null;
            PENDING_WEAR.put(target, new Wear(source, hit.slot(), hit.damage()));
            return amount * (1.0F - reduction(target, hit.slot()));
        }
        if (source.is(ModTags.BALLISTIC) && !isClaimed(source)) {
            EquipmentSlot slot = guessSlot(target, source);
            float reduction = reduction(target, slot);
            if (reduction > 0.0F) {
                PENDING_WEAR.put(target, new Wear(source, slot, amount));
                return amount * (1.0F - reduction);
            }
        }
        return amount;
    }

    /** Whether vanilla armor absorption must ignore the tactical pieces, which already blocked their share by zone. */
    public static boolean excludesOwnArmor(LivingEntity target, DamageSource source) {
        return !target.level().isClientSide && isBallistic(source) && !source.is(DamageTypeTags.BYPASSES_ARMOR) && ownArmor(target) > 0;
    }

    /** The armor value vanilla absorption should use: without the tactical pieces when they already blocked by zone. */
    public static int armorWithoutOwn(LivingEntity target, DamageSource source, int armor) {
        return excludesOwnArmor(target, source) ? Math.max(0, armor - (int) ownArmor(target)) : armor;
    }

    /** Damage absorbed by the target's armor without the tactical pieces. */
    public static float armorAbsorptionWithoutOwn(LivingEntity target, float damage, DamageSource source) {
        float armor = Math.max(0.0F, target.getArmorValue() - ownArmor(target));
        float toughness = (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        //? if >=1.21 {
        return damage - CombatRules.getDamageAfterAbsorb(target, damage, source, armor, toughness);
        //?} else {
        /*return damage - CombatRules.getDamageAfterAbsorb(damage, armor, toughness);
        *///?}
    }

    /** Called once damage was actually dealt, after shields and invulnerability frames. */
    public static void onDamageDealt(LivingEntity entity, DamageSource source, float blockedDamage) {
        Wear wear = PENDING_WEAR.get(entity);
        if (wear != null && wear.source() == source) {
            PENDING_WEAR.remove(entity);
            if (blockedDamage <= 0.0F) {
                wear(entity, wear.slot(), wear.damage());
                if (wear.slot() == EquipmentSlot.HEAD && entity.isAlive()) {
                    ModAdvancements.award(entity, ModAdvancements.HELMET_SAVE);
                }
            }
        }
    }

    /** Tactical pieces wear by hit zone instead of the vanilla per-hit armor damage. */
    public static boolean skipsVanillaWear(DamageSource source, ItemStack armor) {
        return isBallistic(source) && !source.is(DamageTypeTags.BYPASSES_ARMOR) && armor.getItem() instanceof TacticalArmorItem;
    }

    public static void onServerStopped() {
        expected = null;
        PENDING_WEAR.clear();
    }

    private static float ownArmor(LivingEntity entity) {
        float total = 0.0F;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.isArmor() && entity.getItemBySlot(slot).getItem() instanceof TacticalArmorItem armor
                    && armor.getEquipmentSlot() == slot) {
                total += armor.getDefense();
            }
        }
        return total;
    }
}
