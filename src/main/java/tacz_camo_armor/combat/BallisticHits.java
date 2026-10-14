package tacz_camo_armor.combat;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.config.ServerConfig;
import tacz_camo_armor.item.TacticalArmorItem;
import tacz_camo_armor.registry.ModTags;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/**
 * Zone-based protection against bullets and other projectiles.
 * <p>
 * Without a gun mod integration, every damage type in {@code #tacz_camo_armor:ballistic} (all projectiles by default)
 * is reduced by the armor piece covering the hit zone, worked out from the projectile's path or, for hitscan weapons,
 * from the shooter's line of sight. Integrations that know the exact hit call {@link #expect} right before the damage
 * is dealt and {@link #claim} their damage types so the generic path leaves them alone.
 */
@EventBusSubscriber(modid = TaczCamoArmor.MOD_ID)
public final class BallisticHits {
    private static final List<Predicate<DamageSource>> CLAIMS = new CopyOnWriteArrayList<>();
    private static final double HITSCAN_RANGE = 256.0;
    // Set by an integration and consumed by the hurt call that follows it on the same thread.
    private static ExpectedHit expected;

    private record ExpectedHit(LivingEntity target, DamageSource source, EquipmentSlot slot) {
    }

    private BallisticHits() {
    }

    /** Marks damage that an integration handles itself through {@link #expect}. */
    public static void claim(Predicate<DamageSource> handled) {
        CLAIMS.add(handled);
    }

    /**
     * Announces that {@code source} is about to hurt {@code target} on {@code slot}: wears the armor piece in that slot
     * by {@code damage} and reduces the next hurt call with this exact source by the piece's protection.
     */
    public static void expect(LivingEntity target, DamageSource source, EquipmentSlot slot, float damage) {
        expected = null;
        if (target.getItemBySlot(slot).getItem() instanceof TacticalArmorItem) {
            wear(target, slot, damage);
            expected = new ExpectedHit(target, source, slot);
        }
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
        int wear = Math.max(1, Math.round(damage * (float) ServerConfig.durabilityPerDamage()));
        target.getItemBySlot(slot).hurtAndBreak(wear, target, slot);
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

    @SubscribeEvent
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) {
            return;
        }
        ExpectedHit hit = expected;
        if (hit != null && hit.source() == source && hit.target() == target) {
            expected = null;
            event.setAmount(event.getAmount() * (1.0F - reduction(target, hit.slot())));
        } else if (source.is(ModTags.BALLISTIC) && !isClaimed(source)) {
            EquipmentSlot slot = guessSlot(target, source);
            float reduction = reduction(target, slot);
            if (reduction > 0.0F) {
                wear(target, slot, event.getAmount());
                event.setAmount(event.getAmount() * (1.0F - reduction));
            }
        }
        if (!isBallistic(source) || source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            return;
        }
        float ownArmor = ownArmor(target);
        if (ownArmor > 0) {
            // Tactical armor already blocked its share by zone, so its armor points must not count twice.
            event.addReductionModifier(DamageContainer.Reduction.ARMOR, (container, reduction) -> {
                float damage = container.getNewDamage();
                float armor = Math.max(0.0F, target.getArmorValue() - ownArmor);
                float toughness = (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
                return damage - CombatRules.getDamageAfterAbsorb(target, damage, container.getSource(), armor, toughness);
            });
        }
    }

    @SubscribeEvent
    static void onArmorHurt(ArmorHurtEvent event) {
        DamageSource source = event.getDamageSource();
        if (!isBallistic(source) || source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            return;
        }
        event.getArmorMap().forEach((slot, entry) -> {
            if (entry.armorItemStack.getItem() instanceof TacticalArmorItem) {
                entry.newDamage = 0.0F;
            }
        });
    }

    @SubscribeEvent
    static void onServerStopped(ServerStoppedEvent event) {
        expected = null;
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
