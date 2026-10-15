package tactical_loadout.gear;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import tactical_loadout.medical.Downed;

/** Loader-independent handlers for the zip ties, downed state and bomb suit; the loaders only forward their events here. */
public final class GearEvents {
    private GearEvents() {
    }

    /** Damage about to hit an entity: lethal for a downed player, mostly absorbed by a full bomb suit when it is an explosion. */
    public static float incomingDamage(LivingEntity target, DamageSource source, float amount) {
        if (target instanceof ServerPlayer player && Downed.isDowned(player)) {
            return Downed.incomingDamage(player, amount);
        }
        return EodSuit.blastDamage(target, source, amount);
    }

    /** True when the death is replaced by going down. */
    public static boolean preventDeath(LivingEntity entity, DamageSource source) {
        if (!(entity instanceof ServerPlayer player)) {
            return false;
        }
        if (Downed.tryGoDown(player, source)) {
            return true;
        }
        Downed.onDeath(player);
        return false;
    }

    /** Right-click on another player: shears cut zip ties, anything but zip ties helps a downed player up. True when handled. */
    public static boolean onEntityUse(Player actor, Entity target, ItemStack held) {
        if (!(actor instanceof ServerPlayer user) || !(target instanceof ServerPlayer victim)) {
            return false;
        }
        if (held.is(Items.SHEARS)) {
            return Restraints.cutClick(user, victim);
        }
        return !(held.getItem() instanceof ZipTiesItem) && Downed.reviveClick(user, victim);
    }

    public static void onPlayerTick(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            Downed.tick(serverPlayer);
        }
    }

    public static void onLogin(ServerPlayer player) {
        Restraints.onLogin(player);
        Downed.onLogin(player);
    }

    public static void onRespawn(ServerPlayer player) {
        Restraints.onRespawn(player);
        Downed.onRespawn(player);
    }

    public static void onServerStopped() {
        Restraints.onServerStopped();
        Downed.onServerStopped();
    }
}
