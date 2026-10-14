package tacz_camo_armor.client;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.item.TacticalArmorItem;

@SuppressWarnings("deprecation")
public class TacticalArmorModel extends GeoModel<TacticalArmorItem> {
    private static final ResourceLocation ANIMATION = TaczCamoArmor.id("animations/armor.animation.json");

    @Override
    public ResourceLocation getModelResource(TacticalArmorItem item) {
        return TaczCamoArmor.id("geo/armor/" + item.set().form().id() + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(TacticalArmorItem item) {
        return TaczCamoArmor.id("textures/armor/" + item.set().id() + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(TacticalArmorItem item) {
        return ANIMATION;
    }
}
