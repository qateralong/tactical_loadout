package tactical_loadout.gear;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.config.ServerConfig;
import tactical_loadout.network.RestraintPayload;
import tactical_loadout.platform.Platform;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Zip-tied players: they cannot move, jump, attack or use anything until they struggle free or someone cuts them loose. */
public final class Restraints {
    public static final int CLICKS = 3;
    // Survives relogs as an entity tag; the struggle progress itself starts over.
    private static final String TAG = TacticalLoadout.MOD_ID + ".tied";
    private static final int CLICK_WINDOW = 40;
    private static final int MAX_STRUGGLES_PER_TICK = 2;
    private static final ResourceLocation MODIFIER = TacticalLoadout.id("zip_tied");
    private static final Map<UUID, Progress> PROGRESS = new HashMap<>();
    private static final Map<UUID, Integer> REMAINING = new HashMap<>();
    private static final Map<UUID, long[]> STRUGGLE_RATE = new HashMap<>();
    // The local player's remaining struggles, set from the server; 0 when free.
    private static int clientRemaining;

    private record Progress(UUID target, boolean cutting, int count, long time) {
    }

    private Restraints() {
    }

    public static boolean isTied(Player player) {
        return player.level().isClientSide ? clientRemaining > 0 : player.getTags().contains(TAG);
    }

    public static int clientRemaining() {
        return clientRemaining;
    }

    public static void setClientRemaining(int remaining) {
        clientRemaining = remaining;
    }

    /** One click with zip ties on another player; the third quick click in a row ties them up. */
    public static void tieClick(ServerPlayer binder, ServerPlayer victim, ItemStack stack) {
        if (isTied(victim)) {
            binder.displayClientMessage(message("already_tied"), true);
            return;
        }
        int count = click(binder, victim, false);
        victim.level().playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.LEASH_KNOT_PLACE, SoundSource.PLAYERS, 0.8F,
                0.9F + 0.15F * count);
        if (count < CLICKS) {
            binder.displayClientMessage(message("tying", count, CLICKS), true);
            return;
        }
        PROGRESS.remove(binder.getUUID());
        tie(victim);
        if (!binder.getAbilities().instabuild) {
            stack.shrink(1);
        }
        binder.displayClientMessage(message("tied_other", victim.getDisplayName()), true);
    }

    /** One click with shears on a tied player; the third quick click in a row cuts them loose. */
    public static boolean cutClick(ServerPlayer cutter, ServerPlayer victim) {
        if (!isTied(victim)) {
            return false;
        }
        int count = click(cutter, victim, true);
        victim.level().playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 0.8F, 1.2F);
        if (count < CLICKS) {
            cutter.displayClientMessage(message("cutting", count, CLICKS), true);
        } else {
            PROGRESS.remove(cutter.getUUID());
            free(victim);
            cutter.displayClientMessage(message("cut_other", victim.getDisplayName()), true);
        }
        return true;
    }

    private static int click(ServerPlayer actor, ServerPlayer victim, boolean cutting) {
        long now = actor.level().getGameTime();
        Progress last = PROGRESS.get(actor.getUUID());
        int count = last != null && last.target().equals(victim.getUUID()) && last.cutting() == cutting && now - last.time() <= CLICK_WINDOW
                ? last.count() + 1 : 1;
        PROGRESS.put(actor.getUUID(), new Progress(victim.getUUID(), cutting, count, now));
        return count;
    }

    public static void tie(ServerPlayer player) {
        player.addTag(TAG);
        REMAINING.put(player.getUUID(), ServerConfig.zipTieStruggles());
        apply(player);
        player.stopUsingItem();
        player.displayClientMessage(message("tied_self").withStyle(ChatFormatting.RED), true);
        sync(player);
    }

    public static void free(ServerPlayer player) {
        player.removeTag(TAG);
        REMAINING.remove(player.getUUID());
        STRUGGLE_RATE.remove(player.getUUID());
        remove(player);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LEASH_KNOT_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
        sync(player);
    }

    /** One press of the struggle key from a tied player. */
    public static void struggle(ServerPlayer player) {
        if (!isTied(player)) {
            return;
        }
        long now = player.level().getGameTime();
        long[] rate = STRUGGLE_RATE.computeIfAbsent(player.getUUID(), id -> new long[]{now, 0});
        if (rate[0] != now) {
            rate[0] = now;
            rate[1] = 0;
        }
        if (++rate[1] > MAX_STRUGGLES_PER_TICK) {
            return;
        }
        int remaining = REMAINING.getOrDefault(player.getUUID(), ServerConfig.zipTieStruggles()) - 1;
        if (remaining <= 0) {
            free(player);
            return;
        }
        REMAINING.put(player.getUUID(), remaining);
        if (remaining % 10 == 0) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LEASH_KNOT_PLACE, SoundSource.PLAYERS, 0.5F,
                    0.6F + player.getRandom().nextFloat() * 0.3F);
        }
        sync(player);
    }

    public static void onLogin(ServerPlayer player) {
        if (player.getTags().contains(TAG)) {
            REMAINING.put(player.getUUID(), ServerConfig.zipTieStruggles());
            apply(player);
        }
        sync(player);
    }

    /** A respawned player starts free. */
    public static void onRespawn(ServerPlayer player) {
        player.removeTag(TAG);
        REMAINING.remove(player.getUUID());
        remove(player);
        sync(player);
    }

    public static void onServerStopped() {
        PROGRESS.clear();
        REMAINING.clear();
        STRUGGLE_RATE.clear();
    }

    private static void apply(ServerPlayer player) {
        modify(player, Attributes.MOVEMENT_SPEED, true);
        modify(player, Attributes.JUMP_STRENGTH, true);
    }

    private static void remove(ServerPlayer player) {
        modify(player, Attributes.MOVEMENT_SPEED, false);
        modify(player, Attributes.JUMP_STRENGTH, false);
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
        Platform.INSTANCE.sendToPlayer(player, new RestraintPayload(isTied(player) ? REMAINING.getOrDefault(player.getUUID(), 0) : 0));
    }

    private static MutableComponent message(String key, Object... args) {
        return Component.translatable("message." + TacticalLoadout.MOD_ID + ".zip_ties." + key, args);
    }
}
