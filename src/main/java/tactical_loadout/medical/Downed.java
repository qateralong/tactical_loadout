package tactical_loadout.medical;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.config.ServerConfig;
import tactical_loadout.network.DownedPayload;
import tactical_loadout.platform.Platform;
import tactical_loadout.registry.ModItems;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Rust-style downed state, off unless enabled in the server config: lethal damage knocks a player down instead of killing them.
 * They crawl and bleed out unless someone holds right-click on them; any further damage finishes them.
 */
public final class Downed {
    // Survives relogs as an entity tag, so logging out while down still ends in death.
    private static final String TAG = TacticalLoadout.MOD_ID + ".downed";
    private static final ResourceLocation MODIFIER = TacticalLoadout.id("downed");
    // Holding right-click on an entity repeats the interaction every 4 ticks; a longer pause starts the revive over.
    private static final int REVIVE_GAP = 8;
    private static final Map<UUID, Integer> TICKS_LEFT = new HashMap<>();
    private static final Map<UUID, Revive> REVIVES = new HashMap<>();
    // The local player's remaining bleed-out ticks, counted down between server updates; 0 when not down.
    private static int clientTicks;

    private record Revive(UUID target, int progress, long lastTick) {
    }

    private Downed() {
    }

    public static boolean isDowned(Player player) {
        return player.level().isClientSide ? clientTicks > 0 : player.getTags().contains(TAG);
    }

    public static int clientTicks() {
        return clientTicks;
    }

    public static void setClientTicks(int ticks) {
        clientTicks = ticks;
    }

    public static void clientTick() {
        if (clientTicks > 1) {
            clientTicks--;
        }
    }

    /** From the death event: true when the player goes down instead of dying. */
    public static boolean tryGoDown(ServerPlayer player, DamageSource source) {
        if (!ServerConfig.downedEnabled() || isDowned(player) || player.isCreative() || player.isSpectator()
                || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        player.addTag(TAG);
        player.setHealth(1.0F);
        TICKS_LEFT.put(player.getUUID(), ServerConfig.downedSeconds() * 20);
        Medical.stopBleeding(player);
        player.stopUsingItem();
        player.setForcedPose(Pose.SWIMMING);
        modify(player, Attributes.JUMP_STRENGTH, true);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1.0F, 0.6F);
        player.displayClientMessage(message("down_self").withStyle(ChatFormatting.RED), true);
        sync(player);
        return true;
    }

    /** Any damage to a downed player is lethal. */
    public static float incomingDamage(ServerPlayer player, float amount) {
        return isDowned(player) ? Math.max(amount, player.getHealth() + player.getAbsorptionAmount() + 100.0F) : amount;
    }

    public static void tick(ServerPlayer player) {
        if (!isDowned(player)) {
            return;
        }
        player.setForcedPose(Pose.SWIMMING);
        int left = TICKS_LEFT.getOrDefault(player.getUUID(), 1) - 1;
        TICKS_LEFT.put(player.getUUID(), left);
        if (left <= 0) {
            if (player.getRandom().nextDouble() < ServerConfig.selfReviveChance()) {
                revive(player, ServerConfig.reviveHealth());
                player.displayClientMessage(message("self_revived").withStyle(ChatFormatting.GREEN), true);
            } else {
                bleedOut(player);
            }
        } else if (left % 20 == 0) {
            sync(player);
        }
    }

    private static void bleedOut(ServerPlayer player) {
        player.invulnerableTime = 0;
        player.hurt(Medical.bleedingSource((ServerLevel) player.level()), Float.MAX_VALUE);
    }

    /** Right-click from another player, repeated while held; returns true when the target is down. */
    public static boolean reviveClick(ServerPlayer reviver, ServerPlayer target) {
        if (!isDowned(target)) {
            return false;
        }
        long now = reviver.level().getGameTime();
        Revive last = REVIVES.get(reviver.getUUID());
        int progress = 0;
        if (last != null && last.target().equals(target.getUUID()) && now - last.lastTick() <= REVIVE_GAP) {
            progress = last.progress() + (int) (now - last.lastTick());
        }
        ItemStack held = reviver.getMainHandItem();
        boolean medkit = held.is(ModItems.MEDKIT.get());
        int needed = ServerConfig.reviveSeconds() * 20 / (medkit ? 2 : 1);
        if (progress >= needed) {
            REVIVES.remove(reviver.getUUID());
            if (medkit && !reviver.getAbilities().instabuild) {
                held.shrink(1);
            }
            revive(target, medkit ? (float) Math.max(ServerConfig.reviveHealth(), ServerConfig.medkitHealth()) : ServerConfig.reviveHealth());
            reviver.displayClientMessage(message("revived_other", target.getDisplayName()).withStyle(ChatFormatting.GREEN), true);
            target.displayClientMessage(message("revived_self", reviver.getDisplayName()).withStyle(ChatFormatting.GREEN), true);
            return true;
        }
        REVIVES.put(reviver.getUUID(), new Revive(target.getUUID(), progress, now));
        int percent = Math.min(99, progress * 100 / needed);
        reviver.displayClientMessage(message("reviving", target.getDisplayName(), percent), true);
        target.displayClientMessage(message("being_revived", reviver.getDisplayName(), percent), true);
        return true;
    }

    private static void revive(ServerPlayer player, float health) {
        clear(player);
        player.setHealth(Math.min(player.getMaxHealth(), health));
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.PLAYERS,
                1.0F, 0.8F);
    }

    private static void clear(ServerPlayer player) {
        player.removeTag(TAG);
        TICKS_LEFT.remove(player.getUUID());
        player.setForcedPose(null);
        modify(player, Attributes.JUMP_STRENGTH, false);
        sync(player);
    }

    /** A player who logged out while down bleeds out right after joining again. */
    public static void onLogin(ServerPlayer player) {
        if (player.getTags().contains(TAG)) {
            TICKS_LEFT.put(player.getUUID(), 1);
            player.setForcedPose(Pose.SWIMMING);
        }
        sync(player);
    }

    public static void onRespawn(ServerPlayer player) {
        clear(player);
    }

    public static void onServerStopped() {
        TICKS_LEFT.clear();
        REVIVES.clear();
    }

    private static void modify(ServerPlayer player, Holder<Attribute> attribute, boolean add) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        instance.removeModifier(MODIFIER);
        if (add) {
            instance.addTransientModifier(new AttributeModifier(MODIFIER, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    private static void sync(ServerPlayer player) {
        Platform.INSTANCE.sendToPlayer(player, new DownedPayload(isDowned(player) ? TICKS_LEFT.getOrDefault(player.getUUID(), 0) : 0));
    }

    private static MutableComponent message(String key, Object... args) {
        return Component.translatable("message." + TacticalLoadout.MOD_ID + ".downed." + key, args);
    }
}
