package tacz_camo_armor.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
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
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.client.TacticalArmorRenderer;
import tacz_camo_armor.config.ServerConfig;
import tacz_camo_armor.registry.ModArmorMaterials;

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
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        ItemAttributeModifiers modifiers = super.getDefaultAttributeModifiers(stack);
        if (tier != ArmorTier.HEAVY) {
            return modifiers;
        }
        double slowdown = ServerConfig.heavySlowdownPerPiece();
        if (slowdown <= 0) {
            return modifiers;
        }
        AttributeModifier modifier = new AttributeModifier(
                TaczCamoArmor.id("heavy_slowdown_" + piece.id()), -slowdown,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        return modifiers.withModifierAdded(Attributes.MOVEMENT_SPEED, modifier, EquipmentSlotGroup.bySlot(getEquipmentSlot()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        long percent = Math.round(ServerConfig.bulletReduction(tier) * 100);
        Component zone = Component.translatable("tooltip." + TaczCamoArmor.MOD_ID + ".zone." + piece.id());
        tooltip.add(Component.translatable("tooltip." + TaczCamoArmor.MOD_ID + ".bullet_protection", percent, zone)
                .withStyle(ChatFormatting.GRAY));
        if (set.camo() != null) {
            double strength = set.camo().strength();
            long normal = Math.round(Math.min(0.95, ServerConfig.detectionReduction() * strength) * 100);
            long sneaking = Math.round(Math.min(0.95, ServerConfig.sneakingDetectionReduction() * strength) * 100);
            tooltip.add(Component.translatable("tooltip." + TaczCamoArmor.MOD_ID + ".camo", normal, sneaking)
                    .withStyle(ChatFormatting.DARK_GREEN));
            tooltip.add(Component.translatable("tooltip." + TaczCamoArmor.MOD_ID + ".camo_biome." + set.id())
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
}
