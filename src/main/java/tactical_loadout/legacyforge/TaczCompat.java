package tactical_loadout.legacyforge;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.event.common.GunDamageSourcePart;
import com.tacz.guns.init.ModDamageTypes;
import com.tacz.guns.util.HitboxHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.LogicalSide;
import tactical_loadout.combat.BallisticHits;

/** Exact hit zones for TaCZ bullets, including its own headshot detection and armor-piercing split. */
public final class TaczCompat {
    private TaczCompat() {
    }

    public static void init() {
        BallisticHits.claim(source -> source.is(ModDamageTypes.BULLETS_TAG));
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, TaczCompat::onGunHurt);
    }

    private static void onGunHurt(EntityHurtByGunEvent.Pre event) {
        BallisticHits.cancelExpected();
        Entity hurt = event.getHurtEntity();
        if (event.getLogicalSide() != LogicalSide.SERVER || hurt == null) {
            return;
        }
        Entity core = hurt instanceof PartEntity<?> part ? part.getParent() : hurt;
        if (!(core instanceof LivingEntity target)) {
            return;
        }
        EquipmentSlot slot = event.isHeadShot() ? EquipmentSlot.HEAD : bodySlot(event.getBullet(), hurt);
        float damage = event.getBaseAmount() * (event.isHeadShot() ? event.getHeadshotMultiplier() : 1.0F);
        BallisticHits.expect(target, event.getDamageSource(GunDamageSourcePart.NON_ARMOR_PIERCING), slot, damage);
    }

    private static EquipmentSlot bodySlot(Entity bullet, Entity hurt) {
        Entity owner = bullet instanceof Projectile projectile ? projectile.getOwner() : null;
        AABB box = HitboxHelper.getFixedBoundingBox(hurt, owner);
        Vec3 start = bullet.position();
        double y = BallisticHits.hitY(box, start, start.add(bullet.getDeltaMovement()));
        return BallisticHits.bodyZoneSlot((y - box.minY) / box.getYsize());
    }
}
