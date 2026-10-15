package tactical_loadout.client;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.item.TacticalArmorItem;

@SuppressWarnings("deprecation")
public class TacticalArmorModel extends GeoModel<TacticalArmorItem> {
    private static final ResourceLocation ANIMATION = TacticalLoadout.id("animations/armor.animation.json");

    @Override
    public ResourceLocation getModelResource(TacticalArmorItem item) {
        return TacticalLoadout.id("geo/armor/" + item.set().form().id() + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(TacticalArmorItem item) {
        return TacticalLoadout.id("textures/armor/" + item.set().id() + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(TacticalArmorItem item) {
        return ANIMATION;
    }
}
