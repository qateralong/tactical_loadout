package tacz_camo_armor.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import tacz_camo_armor.item.ArmorTier;

public final class ServerConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.DoubleValue LIGHT_REDUCTION;
    private static final ModConfigSpec.DoubleValue MEDIUM_REDUCTION;
    private static final ModConfigSpec.DoubleValue HEAVY_REDUCTION;
    private static final ModConfigSpec.DoubleValue DURABILITY_PER_DAMAGE;
    private static final ModConfigSpec.DoubleValue BOOTS_ZONE_TOP;
    private static final ModConfigSpec.DoubleValue LEGS_ZONE_TOP;
    private static final ModConfigSpec.DoubleValue HEAVY_SLOWDOWN_PER_PIECE;
    private static final ModConfigSpec.DoubleValue DETECTION_REDUCTION;
    private static final ModConfigSpec.DoubleValue SNEAKING_DETECTION_REDUCTION;
    private static final ModConfigSpec.DoubleValue FAVORED_BIOME_FACTOR;
    private static final ModConfigSpec.DoubleValue UNFAVORED_BIOME_FACTOR;
    private static final ModConfigSpec.DoubleValue GHILLIE_STRENGTH;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.translation(key("protection")).push("protection");
        LIGHT_REDUCTION = reduction(builder, "lightReduction", 0.25);
        MEDIUM_REDUCTION = reduction(builder, "mediumReduction", 0.40);
        HEAVY_REDUCTION = reduction(builder, "heavyReduction", 0.60);
        DURABILITY_PER_DAMAGE = builder
                .comment("Durability lost by the hit piece per point of bullet damage (at least 1 per hit).")
                .translation(key("durabilityPerDamage"))
                .defineInRange("durabilityPerDamage", 1.0, 0.0, 10.0);
        builder.pop();

        builder.translation(key("hitZones")).push("hitZones");
        BOOTS_ZONE_TOP = builder
                .comment("Hits below this fraction of the target's hitbox height land on the boots.")
                .translation(key("bootsZoneTop"))
                .defineInRange("bootsZoneTop", 0.15, 0.0, 1.0);
        LEGS_ZONE_TOP = builder
                .comment("Hits below this fraction (and above the boots zone) land on the leggings; higher non-headshot hits land on the plate carrier.")
                .translation(key("legsZoneTop"))
                .defineInRange("legsZoneTop", 0.40, 0.0, 1.0);
        builder.pop();

        builder.translation(key("mobility")).push("mobility");
        HEAVY_SLOWDOWN_PER_PIECE = builder
                .comment("Movement speed penalty for each worn heavy-tier piece, as a fraction of base speed (0.03 = -3%).")
                .translation(key("heavySlowdownPerPiece"))
                .defineInRange("heavySlowdownPerPiece", 0.03, 0.0, 0.25);
        builder.pop();

        builder.translation(key("camouflage")).push("camouflage");
        DETECTION_REDUCTION = builder
                .comment("How much a full US camo set shrinks the distance at which mobs notice the wearer (0.3 = -30%).")
                .translation(key("detectionReduction"))
                .defineInRange("detectionReduction", 0.30, 0.0, 0.95);
        SNEAKING_DETECTION_REDUCTION = builder
                .comment("Same as detectionReduction, but while sneaking. Stacks with the vanilla sneaking bonus.")
                .translation(key("sneakingDetectionReduction"))
                .defineInRange("sneakingDetectionReduction", 0.50, 0.0, 0.95);
        FAVORED_BIOME_FACTOR = builder
                .comment("Multiplier for the camo effect in biomes matching the pattern (biome tags tacz_camo_armor:camo/*_favored).")
                .translation(key("favoredBiomeFactor"))
                .defineInRange("favoredBiomeFactor", 1.3, 1.0, 3.0);
        UNFAVORED_BIOME_FACTOR = builder
                .comment("Multiplier for the camo effect in biomes clashing with the pattern (biome tags tacz_camo_armor:camo/*_unfavored).")
                .translation(key("unfavoredBiomeFactor"))
                .defineInRange("unfavoredBiomeFactor", 0.5, 0.0, 1.0);
        GHILLIE_STRENGTH = builder
                .comment("Extra multiplier for the ghillie suit camo effect.")
                .translation(key("ghillieStrength"))
                .defineInRange("ghillieStrength", 1.5, 1.0, 3.0);
        builder.pop();

        SPEC = builder.build();
    }

    private ServerConfig() {
    }

    private static String key(String name) {
        return "tacz_camo_armor.configuration." + name;
    }

    private static ModConfigSpec.DoubleValue reduction(ModConfigSpec.Builder builder, String name, double value) {
        return builder
                .comment("Fraction of TACZ bullet damage blocked by a piece of this tier in its hit zone. Armor-piercing ammo lowers it proportionally.")
                .translation(key(name))
                .defineInRange(name, value, 0.0, 1.0);
    }

    private static double get(ModConfigSpec.DoubleValue value) {
        return SPEC.isLoaded() ? value.get() : value.getDefault();
    }

    public static double bulletReduction(ArmorTier tier) {
        return get(switch (tier) {
            case LIGHT -> LIGHT_REDUCTION;
            case MEDIUM -> MEDIUM_REDUCTION;
            case HEAVY -> HEAVY_REDUCTION;
        });
    }

    public static double durabilityPerDamage() {
        return get(DURABILITY_PER_DAMAGE);
    }

    public static double bootsZoneTop() {
        return get(BOOTS_ZONE_TOP);
    }

    public static double legsZoneTop() {
        return Math.max(get(LEGS_ZONE_TOP), bootsZoneTop());
    }

    public static double heavySlowdownPerPiece() {
        return get(HEAVY_SLOWDOWN_PER_PIECE);
    }

    public static double detectionReduction() {
        return get(DETECTION_REDUCTION);
    }

    public static double sneakingDetectionReduction() {
        return get(SNEAKING_DETECTION_REDUCTION);
    }

    public static double favoredBiomeFactor() {
        return get(FAVORED_BIOME_FACTOR);
    }

    public static double unfavoredBiomeFactor() {
        return get(UNFAVORED_BIOME_FACTOR);
    }

    public static double ghillieStrength() {
        return get(GHILLIE_STRENGTH);
    }
}
