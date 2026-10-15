package tactical_loadout.device;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.item.ArmorForm;
import tactical_loadout.item.ArmorPiece;
import tactical_loadout.item.TacticalArmorItem;
import tactical_loadout.registry.ModDataComponents;

import java.util.EnumSet;
import java.util.Set;

public final class Devices {
    public static final Set<ArmorForm> NVG_FORMS = EnumSet.of(ArmorForm.US, ArmorForm.RU, ArmorForm.SWAT, ArmorForm.CT);

    private Devices() {
    }

    public static ModDataComponents.Key<MountedDevice> component(DeviceKind kind) {
        return kind.isNvg() ? ModDataComponents.NVG : ModDataComponents.GAS_MASK;
    }

    public static boolean isHelmet(ItemStack stack) {
        return stack.getItem() instanceof TacticalArmorItem armor && armor.piece() == ArmorPiece.HELMET;
    }

    public static boolean accepts(ItemStack helmet, DeviceKind kind) {
        if (!(helmet.getItem() instanceof TacticalArmorItem armor) || armor.piece() != ArmorPiece.HELMET) {
            return false;
        }
        // The bomb suit helmet has a sealed blast visor with no room for devices.
        if (armor.set().form() == ArmorForm.EOD || kind.isNvg() && !NVG_FORMS.contains(armor.set().form())) {
            return false;
        }
        return !component(kind).has(helmet);
    }

    @Nullable
    public static MountedDevice activeNvg(LivingEntity entity) {
        ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
        MountedDevice nvg = ModDataComponents.NVG.get(head);
        if (nvg == null || !ModDataComponents.NVG_DOWN.getOrDefault(head, false) || !nvg.hasCharge()) {
            return null;
        }
        return nvg;
    }

    public static void toggleNvg(Player player) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        MountedDevice nvg = ModDataComponents.NVG.get(head);
        if (nvg == null) {
            return;
        }
        boolean down = ModDataComponents.NVG_DOWN.getOrDefault(head, false);
        if (!down && !nvg.hasCharge()) {
            player.displayClientMessage(Component.translatable("message." + TacticalLoadout.MOD_ID + ".nvg_empty"), true);
            return;
        }
        ModDataComponents.NVG_DOWN.set(head, !down);
        player.level().playSound(null, player.getX(), player.getEyeY(), player.getZ(),
                down ? SoundEvents.SPYGLASS_STOP_USING : SoundEvents.SPYGLASS_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static Component chargeLine(MountedDevice device) {
        String prefix = "tooltip." + TacticalLoadout.MOD_ID + ".device.";
        if (!device.kind().usesCharge()) {
            return Component.translatable(prefix + "unlimited").withStyle(ChatFormatting.GREEN);
        }
        long percent = Math.round(100.0 * device.clampedCharge() / device.kind().maxCharge());
        String key = prefix + (device.kind().isNvg() ? "battery" : "filter");
        return Component.translatable(key, percent).withStyle(percent > 20 ? ChatFormatting.GREEN : ChatFormatting.RED);
    }
}
