package tactical_loadout.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.client.TacticalArmorRenderer;
import tactical_loadout.config.ServerConfig;
import tactical_loadout.device.Devices;
import tactical_loadout.device.MountedDevice;
import tactical_loadout.registry.ModDataComponents;
import tactical_loadout.registry.ModItems;
import tactical_loadout.registry.ModArmorMaterials;

import java.util.List;
import java.util.function.Consumer;

public class TacticalArmorItem extends ArmorItem implements GeoItem {
    private final ArmorSet set;
    private final ArmorTier tier;
    private final ArmorPiece piece;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public TacticalArmorItem(ArmorSet set, ArmorTier tier, ArmorPiece piece, Properties properties) {
        super(ModArmorMaterials.get(tier), piece.type(),
                properties.durability(piece.type().getDurability(tier.durabilityMultiplier())));
        this.set = set;
        this.tier = tier;
        this.piece = piece;
    }

    public ArmorSet set() {
        return set;
    }

    public ArmorTier tier() {
        return tier;
    }

    public ArmorPiece piece() {
        return piece;
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        ItemAttributeModifiers modifiers = super.getDefaultAttributeModifiers();
        if (tier != ArmorTier.HEAVY) {
            return modifiers;
        }
        double slowdown = ServerConfig.heavySlowdownPerPiece();
        if (slowdown <= 0) {
            return modifiers;
        }
        AttributeModifier modifier = new AttributeModifier(
                TacticalLoadout.id("heavy_slowdown_" + piece.id()), -slowdown,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        return modifiers.withModifierAdded(Attributes.MOVEMENT_SPEED, modifier, EquipmentSlotGroup.bySlot(getEquipmentSlot()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        long percent = Math.round(ServerConfig.bulletReduction(tier) * 100);
        Component zone = Component.translatable("tooltip." + TacticalLoadout.MOD_ID + ".zone." + piece.id());
        tooltip.add(Component.translatable("tooltip." + TacticalLoadout.MOD_ID + ".bullet_protection", percent, zone)
                .withStyle(ChatFormatting.GRAY));
        appendDevices(stack, tooltip);
        if (set.camo() != null) {
            double strength = set.camo().strength();
            long normal = Math.round(Math.min(0.95, ServerConfig.detectionReduction() * strength) * 100);
            long sneaking = Math.round(Math.min(0.95, ServerConfig.sneakingDetectionReduction() * strength) * 100);
            tooltip.add(Component.translatable("tooltip." + TacticalLoadout.MOD_ID + ".camo", normal, sneaking)
                    .withStyle(ChatFormatting.DARK_GREEN));
            tooltip.add(Component.translatable("tooltip." + TacticalLoadout.MOD_ID + ".camo_biome." + set.id())
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private TacticalArmorRenderer renderer;

            @Override
            public <T extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(@Nullable T entity, ItemStack stack,
                                                                                 @Nullable EquipmentSlot slot, @Nullable HumanoidModel<T> original) {
                if (renderer == null) {
                    renderer = new TacticalArmorRenderer();
                }
                return renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    private static void appendDevices(ItemStack stack, List<Component> tooltip) {
        String prefix = "tooltip." + TacticalLoadout.MOD_ID + ".helmet.";
        boolean any = false;
        for (DataComponentType<MountedDevice> type : List.of(ModDataComponents.NVG.get(), ModDataComponents.GAS_MASK.get())) {
            MountedDevice device = stack.get(type);
            if (device == null) {
                continue;
            }
            any = true;
            Component name = Component.translatable(ModItems.device(device.kind()).get().getDescriptionId());
            tooltip.add(Component.translatable(prefix + "mounted", name).withStyle(ChatFormatting.AQUA)
                    .append(" ").append(Devices.chargeLine(device)));
            if (device.kind().isNvg()) {
                tooltip.add(Component.translatable(prefix + "nvg_key", Component.keybind("key." + TacticalLoadout.MOD_ID + ".nvg"))
                        .withStyle(ChatFormatting.GRAY));
            }
        }
        if (any) {
            tooltip.add(Component.translatable(prefix + "detach_hint").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
