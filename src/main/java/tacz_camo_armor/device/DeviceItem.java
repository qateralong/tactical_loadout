package tacz_camo_armor.device;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.registry.ModDataComponents;

import java.util.List;

public class DeviceItem extends Item {
    private final DeviceKind kind;

    public DeviceItem(DeviceKind kind, Properties properties) {
        super(properties.stacksTo(1));
        this.kind = kind;
    }

    public DeviceKind kind() {
        return kind;
    }

    public int charge(ItemStack stack) {
        return Math.min(stack.getOrDefault(ModDataComponents.CHARGE.get(), kind.maxCharge()), kind.maxCharge());
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return kind.usesCharge() && charge(stack) < kind.maxCharge();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * charge(stack) / kind.maxCharge());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return kind.isNvg() ? 0x55FF55 : 0xC8B560;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Devices.chargeLine(new MountedDevice(kind, charge(stack))));
        String prefix = "tooltip." + TaczCamoArmor.MOD_ID + ".device.";
        tooltip.add(Component.translatable(prefix + (kind.isNvg() ? "nvg_helmets" : "mask_effect")).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(prefix + "attach_hint").withStyle(ChatFormatting.DARK_GRAY));
    }
}
