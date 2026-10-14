package tacz_camo_armor.combat;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.event.common.GunDamageSourcePart;
import com.tacz.guns.init.ModDamageTypes;
import com.tacz.guns.util.HitboxHelper;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.config.ServerConfig;
import tacz_camo_armor.item.TacticalArmorItem;

@EventBusSubscriber(modid = TaczCamoArmor.MOD_ID)
public final class BulletProtection {
    // Set by the TACZ pre-hit event and consumed by the non-armor-piercing hurt call that follows it on the same thread.
    private static PendingHit pending;

    private record PendingHit(LivingEntity target, DamageSource source, float reduction) {
    }

    private BulletProtection() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onGunHurt(EntityHurtByGunEvent.Pre event) {
        pending = null;
        Entity hurt = event.getHurtEntity();
        if (event.getLogicalSide() != LogicalSide.SERVER || hurt == null) {
            return;
        }
        Entity core = hurt instanceof PartEntity<?> part ? part.getParent() : hurt;
        if (!(core instanceof LivingEntity target)) {
            return;
        }
        EquipmentSlot slot = event.isHeadShot() ? EquipmentSlot.HEAD : bodySlot(event.getBullet(), hurt);
        ItemStack stack = target.getItemBySlot(slot);
        if (!(stack.getItem() instanceof TacticalArmorItem armor)) {
            return;
        }
        pending = new PendingHit(target, event.getDamageSource(GunDamageSourcePart.NON_ARMOR_PIERCING),
                (float) ServerConfig.bulletReduction(armor.tier()));
        float damage = event.getBaseAmount() * (event.isHeadShot() ? event.getHeadshotMultiplier() : 1.0F);
        int wear = Math.max(1, Math.round(damage * (float) ServerConfig.durabilityPerDamage()));
        stack.hurtAndBreak(wear, target, slot);
    }

    public static EquipmentSlot bodySlot(Entity bullet, Entity hurt) {
        Entity owner = bullet instanceof Projectile projectile ? projectile.getOwner() : null;
        AABB box = HitboxHelper.getFixedBoundingBox(hurt, owner);
        Vec3 start = bullet.position();
        Vec3 end = start.add(bullet.getDeltaMovement());
        double y = box.clip(start, end).map(hit -> hit.y).orElse(Mth.clamp(start.y, box.minY, box.maxY));
        double height = (y - box.minY) / box.getYsize();
        if (height < ServerConfig.bootsZoneTop()) {
            return EquipmentSlot.FEET;
        }
        return height < ServerConfig.legsZoneTop() ? EquipmentSlot.LEGS : EquipmentSlot.CHEST;
    }

    @SubscribeEvent
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();
        LivingEntity target = event.getEntity();
        PendingHit hit = pending;
        if (hit != null && hit.source() == source && hit.target() == target) {
            pending = null;
            event.setAmount(event.getAmount() * (1.0F - hit.reduction()));
        }
        if (!isArmoredBullet(source)) {
            return;
        }
        float ownArmor = ownArmor(target);
        if (ownArmor > 0) {
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
        if (!isArmoredBullet(event.getDamageSource())) {
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
        pending = null;
    }

    private static boolean isArmoredBullet(DamageSource source) {
        return source.is(ModDamageTypes.BULLETS_TAG) && !source.is(DamageTypeTags.BYPASSES_ARMOR);
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
