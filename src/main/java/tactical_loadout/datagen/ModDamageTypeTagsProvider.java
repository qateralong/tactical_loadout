package tactical_loadout.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.grenade.IncendiaryFire;
import tactical_loadout.medical.Medical;
import tactical_loadout.registry.ModTags;

import java.util.concurrent.CompletableFuture;

public class ModDamageTypeTagsProvider extends TagsProvider<DamageType> {
    public ModDamageTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper fileHelper) {
        super(output, Registries.DAMAGE_TYPE, lookup, TacticalLoadout.MOD_ID, fileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Gun mods whose bullet damage is not tagged as a projectile.
        tag(ModTags.BALLISTIC).addTag(DamageTypeTags.IS_PROJECTILE)
                .addOptional(ResourceLocation.fromNamespaceAndPath("thepa", "shot"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("thepa", "pulse"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("gbg", "bullet_light"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("gbg", "bullet_standard"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("gbg", "bullet_heavy"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("superbwarfare", "gunfire"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("superbwarfare", "gunfire_headshot"));
        tag(DamageTypeTags.IS_FIRE).add(IncendiaryFire.DAMAGE);
        tag(DamageTypeTags.BYPASSES_ARMOR).add(Medical.BLEEDING_DAMAGE);
        tag(DamageTypeTags.BYPASSES_SHIELD).add(Medical.BLEEDING_DAMAGE);
        tag(DamageTypeTags.NO_KNOCKBACK).add(Medical.BLEEDING_DAMAGE);
        tag(DamageTypeTags.NO_IMPACT).add(Medical.BLEEDING_DAMAGE);
        tag(DamageTypeTags.NO_KNOCKBACK).add(IncendiaryFire.DAMAGE);
        tag(DamageTypeTags.BURNS_ARMOR_STANDS).add(IncendiaryFire.DAMAGE);
    }
}
