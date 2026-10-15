package tactical_loadout.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
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
//? if >=1.21 {
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
//?} else {
/*import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.world.entity.ai.attributes.Attribute;
*///?}

public class TacticalArmorItem extends ArmorItem implements GeoItem {
    private final ArmorSet set;
    private final ArmorTier tier;
    private final ArmorPiece piece;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public TacticalArmorItem(ArmorSet set, ArmorTier tier, ArmorPiece piece, Properties properties) {
        super(ModArmorMaterials.get(tier), piece.type(),
                properties.durability(piece.durability(tier.durabilityMultiplier())));
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

    //? if >=1.21 {
    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        ItemAttributeModifiers modifiers = super.getDefaultAttributeModifiers();
        EquipmentSlotGroup group = EquipmentSlotGroup.bySlot(getEquipmentSlot());
        if (set == ArmorSet.EOD) {
            // Each piece carries a quarter of the full suit's weight.
            modifiers = modifiers
                    .withModifierAdded(Attributes.MOVEMENT_SPEED, new AttributeModifier(TacticalLoadout.id("eod_slowdown_" + piece.id()),
                            -ServerConfig.eodSlowdown() / 4, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), group)
                    .withModifierAdded(Attributes.JUMP_STRENGTH, new AttributeModifier(TacticalLoadout.id("eod_jump_" + piece.id()),
                            -ServerConfig.eodJumpReduction() / 4, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), group);
        }
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
        return modifiers.withModifierAdded(Attributes.MOVEMENT_SPEED, modifier, group);
    }
    //?} else {
    /*@Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        Multimap<Attribute, AttributeModifier> modifiers = super.getDefaultAttributeModifiers(slot);
        if (tier != ArmorTier.HEAVY || slot != getEquipmentSlot()) {
            return modifiers;
        }
        double slowdown = ServerConfig.heavySlowdownPerPiece();
        if (slowdown <= 0) {
            return modifiers;
        }
        UUID id = UUID.nameUUIDFromBytes(TacticalLoadout.id("heavy_slowdown_" + piece.id()).toString().getBytes());
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.putAll(modifiers);
        builder.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(id, "Heavy armor slowdown", -slowdown,
                AttributeModifier.Operation.MULTIPLY_BASE));
        return builder.build();
    }
    *///?}

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        long percent = Math.round(ServerConfig.bulletReduction(tier) * 100);
        Component zone = Component.translatable("tooltip." + TacticalLoadout.MOD_ID + ".zone." + piece.id());
        tooltip.add(Component.translatable("tooltip." + TacticalLoadout.MOD_ID + ".bullet_protection", percent, zone)
                .withStyle(ChatFormatting.GRAY));
        if (set == ArmorSet.EOD) {
            tooltip.add(Component.translatable("tooltip." + TacticalLoadout.MOD_ID + ".eod", Math.round(ServerConfig.eodBlastReduction() * 100))
                    .withStyle(ChatFormatting.GOLD));
        }
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

    //? if >=1.21 {
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
    //?} elif fabric {
    /*private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new software.bernie.geckolib.animatable.client.RenderProvider() {
            private TacticalArmorRenderer renderer;

            @Override
            public HumanoidModel<LivingEntity> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot,
                                                                     HumanoidModel<LivingEntity> original) {
                if (renderer == null) {
                    renderer = new TacticalArmorRenderer();
                }
                renderer.prepForRender(entity, stack, slot, original);
                return renderer;
            }
        });
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return renderProvider;
    }
    *///?} else {
    /*@Override
    public void initializeClient(Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        consumer.accept(new net.minecraftforge.client.extensions.common.IClientItemExtensions() {
            private TacticalArmorRenderer renderer;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot,
                                                          HumanoidModel<?> original) {
                if (renderer == null) {
                    renderer = new TacticalArmorRenderer();
                }
                renderer.prepForRender(entity, stack, slot, original);
                return renderer;
            }
        });
    }
    *///?}

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
        for (ModDataComponents.Key<MountedDevice> key : List.of(ModDataComponents.NVG, ModDataComponents.GAS_MASK)) {
            MountedDevice device = key.get(stack);
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
