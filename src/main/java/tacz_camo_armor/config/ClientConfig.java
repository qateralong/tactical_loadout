package tacz_camo_armor.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClientConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.BooleanValue THROW_FOV_EFFECT;
    private static final ModConfigSpec.DoubleValue FLASH_MAX_OPACITY;
    private static final ModConfigSpec.BooleanValue NVG_VIGNETTE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.translation(key("visuals")).push("visuals");
        THROW_FOV_EFFECT = builder
                .comment("Widen the field of view while winding up a grenade throw.")
                .translation(key("throwFovEffect"))
                .define("throwFovEffect", true);
        FLASH_MAX_OPACITY = builder
                .comment("Strongest white-out a flashbang can cause on your screen. Lower it if bright flashes bother you.")
                .translation(key("flashMaxOpacity"))
                .defineInRange("flashMaxOpacity", 1.0, 0.3, 1.0);
        NVG_VIGNETTE = builder
                .comment("Darken the screen around the night vision and thermal tubes.")
                .translation(key("nvgVignette"))
                .define("nvgVignette", true);
        builder.pop();
        SPEC = builder.build();
    }

    private ClientConfig() {
    }

    private static String key(String name) {
        return "tacz_camo_armor.configuration." + name;
    }

    public static boolean throwFovEffect() {
        return SPEC.isLoaded() ? THROW_FOV_EFFECT.get() : THROW_FOV_EFFECT.getDefault();
    }

    public static float flashMaxOpacity() {
        double value = SPEC.isLoaded() ? FLASH_MAX_OPACITY.get() : FLASH_MAX_OPACITY.getDefault();
        return (float) value;
    }

    public static boolean nvgVignette() {
        return SPEC.isLoaded() ? NVG_VIGNETTE.get() : NVG_VIGNETTE.getDefault();
    }
}
