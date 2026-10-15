package tacz_camo_armor.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.grenade.IncendiaryFire;
import tacz_camo_armor.registry.ModTags;

import java.util.concurrent.CompletableFuture;

public class ModDamageTypeTagsProvider extends TagsProvider<DamageType> {
    public ModDamageTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper fileHelper) {
        super(output, Registries.DAMAGE_TYPE, lookup, TaczCamoArmor.MOD_ID, fileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Gun mods whose bullet damage is not tagged as a projectile.
        tag(ModTags.BALLISTIC).addTag(DamageTypeTags.IS_PROJECTILE)
                .addOptional(ResourceLocation.fromNamespaceAndPath("thepa", "shot"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("thepa", "pulse"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("gbg", "bullet_light"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("gbg", "bullet_standard"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("gbg", "bullet_heavy"));
        tag(DamageTypeTags.IS_FIRE).add(IncendiaryFire.DAMAGE);
        tag(DamageTypeTags.NO_KNOCKBACK).add(IncendiaryFire.DAMAGE);
        tag(DamageTypeTags.BURNS_ARMOR_STANDS).add(IncendiaryFire.DAMAGE);
    }
}
