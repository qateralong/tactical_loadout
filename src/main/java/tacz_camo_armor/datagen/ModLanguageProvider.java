package tacz_camo_armor.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import tacz_camo_armor.TaczCamoArmor;

public abstract class ModLanguageProvider extends LanguageProvider {
    protected ModLanguageProvider(PackOutput output, String locale) {
        super(output, TaczCamoArmor.MOD_ID, locale);
    }

    public static final class English extends ModLanguageProvider {
        public English(PackOutput output) {
            super(output, "en_us");
        }

        @Override
        protected void addTranslations() {
            add("itemGroup." + TaczCamoArmor.MOD_ID, "Tactical Camo Armor");
        }
    }

    public static final class Russian extends ModLanguageProvider {
        public Russian(PackOutput output) {
            super(output, "ru_ru");
        }

        @Override
        protected void addTranslations() {
            add("itemGroup." + TaczCamoArmor.MOD_ID, "Тактическая камуфляжная броня");
        }
    }
}
