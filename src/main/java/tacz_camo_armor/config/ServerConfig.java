package tacz_camo_armor.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ServerConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.DoubleValue HEAVY_SLOWDOWN_PER_PIECE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.translation("tacz_camo_armor.configuration.mobility").push("mobility");
        HEAVY_SLOWDOWN_PER_PIECE = builder
                .comment("Movement speed penalty for each worn heavy-tier piece, as a fraction of base speed (0.03 = -3%).")
                .translation("tacz_camo_armor.configuration.heavySlowdownPerPiece")
                .defineInRange("heavySlowdownPerPiece", 0.03, 0.0, 0.25);
        builder.pop();
        SPEC = builder.build();
    }

    private ServerConfig() {
    }

    public static double heavySlowdownPerPiece() {
        return SPEC.isLoaded() ? HEAVY_SLOWDOWN_PER_PIECE.get() : HEAVY_SLOWDOWN_PER_PIECE.getDefault();
    }
}
