package tacz_camo_armor.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.item.ArmorPiece;
import tacz_camo_armor.item.ArmorSet;
import tacz_camo_armor.item.ArmorTier;
import tacz_camo_armor.registry.ModItems;
import tacz_camo_armor.registry.ModTags;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        buildFabrics(output);
        buildComponents(output);
        for (ArmorSet set : ArmorSet.values()) {
            for (ArmorTier tier : set.tiers()) {
                for (ArmorPiece piece : ArmorPiece.values()) {
                    buildArmor(output, set, tier, piece);
                }
            }
        }
    }

    private static List<TagKey<Item>> fabricDyes(ArmorSet set) {
        return switch (set) {
            case CT -> List.of(Tags.Items.DYES_BLUE);
            case T -> List.of(Tags.Items.DYES_WHITE, Tags.Items.DYES_BROWN);
            case US_WOODLAND -> List.of(Tags.Items.DYES_GREEN, Tags.Items.DYES_BROWN);
            case US_DESERT -> List.of(Tags.Items.DYES_YELLOW, Tags.Items.DYES_BROWN);
            case US_GREEN -> List.of(Tags.Items.DYES_GREEN);
            case US_MULTICAM -> List.of(Tags.Items.DYES_YELLOW, Tags.Items.DYES_GREEN, Tags.Items.DYES_BROWN);
            case US_SNOW -> List.of(Tags.Items.DYES_WHITE, Tags.Items.DYES_LIGHT_GRAY);
            case RU_EMR -> List.of(Tags.Items.DYES_GREEN, Tags.Items.DYES_BROWN, Tags.Items.DYES_BLACK);
            case SWAT -> List.of(Tags.Items.DYES_BLACK);
            case GHILLIE -> List.of();
        };
    }

    private void buildFabrics(RecipeOutput output) {
        for (ArmorSet set : ArmorSet.values()) {
            if (set == ArmorSet.GHILLIE) {
                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.fabric(set), 2)
                        .requires(ModItems.fabric(ArmorSet.US_GREEN))
                        .requires(Ingredient.of(ItemTags.LEAVES), 4)
                        .requires(Tags.Items.STRINGS)
                        .requires(Tags.Items.STRINGS)
                        .unlockedBy("has_olive_fabric", has(ModItems.fabric(ArmorSet.US_GREEN)))
                        .save(output);
                continue;
            }
            ShapelessRecipeBuilder builder = ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.fabric(set), 2)
                    .requires(ItemTags.WOOL)
                    .requires(ItemTags.WOOL)
                    .requires(Tags.Items.STRINGS)
                    .requires(Tags.Items.STRINGS);
            for (TagKey<Item> dye : fabricDyes(set)) {
                builder.requires(dye);
            }
            builder.unlockedBy("has_wool", has(ItemTags.WOOL)).save(output);
        }
    }

    private void buildComponents(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.KEVLAR_INSERT)
                .pattern("F")
                .pattern("I")
                .pattern("F")
                .define('F', ModTags.FABRICS)
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("has_fabric", has(ModTags.FABRICS))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RAW_CERAMIC_PLATE)
                .pattern("CCC")
                .pattern("CCC")
                .define('C', Items.CLAY_BALL)
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .save(output);

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.RAW_CERAMIC_PLATE), RecipeCategory.MISC,
                        ModItems.CERAMIC_PLATE, 0.3F, 200)
                .unlockedBy("has_raw_ceramic_plate", has(ModItems.RAW_CERAMIC_PLATE))
                .save(output, TaczCamoArmor.id("ceramic_plate_from_smelting"));
    }

    private static String[] pattern(ArmorTier tier, ArmorPiece piece) {
        return switch (piece) {
            case HELMET -> switch (tier) {
                case LIGHT -> new String[]{"FKF", "F F"};
                case MEDIUM -> new String[]{"KKK", "F F"};
                case HEAVY -> new String[]{"KCK", "F F"};
            };
            case PLATE_CARRIER -> switch (tier) {
                case LIGHT -> new String[]{"F F", "FKF", "FFF"};
                case MEDIUM -> new String[]{"F F", "KKK", "FKF"};
                case HEAVY -> new String[]{"F F", "KCK", "FKF"};
            };
            case LEGGINGS -> switch (tier) {
                case LIGHT -> new String[]{"FKF", "F F", "F F"};
                case MEDIUM -> new String[]{"KKK", "F F", "K K"};
                case HEAVY -> new String[]{"KCK", "F F", "K K"};
            };
            case BOOTS -> switch (tier) {
                case LIGHT -> new String[]{"F F", "K K"};
                case MEDIUM -> new String[]{"K K", "KFK"};
                case HEAVY -> new String[]{"K K", "CFC"};
            };
        };
    }

    private void buildArmor(RecipeOutput output, ArmorSet set, ArmorTier tier, ArmorPiece piece) {
        String[] rows = pattern(tier, piece);
        String keys = String.join("", rows);
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.armor(set, tier, piece));
        for (String row : rows) {
            builder.pattern(row);
        }
        builder.define('F', ModItems.fabric(set));
        if (keys.indexOf('K') >= 0) {
            builder.define('K', ModItems.KEVLAR_INSERT);
        }
        if (keys.indexOf('C') >= 0) {
            builder.define('C', ModItems.CERAMIC_PLATE);
        }
        builder.unlockedBy("has_fabric", has(ModItems.fabric(set))).save(output);
    }
}
