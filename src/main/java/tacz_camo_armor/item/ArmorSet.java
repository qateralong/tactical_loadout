package tacz_camo_armor.item;

import org.jetbrains.annotations.Nullable;
import tacz_camo_armor.camo.CamoPattern;

import java.util.List;

public enum ArmorSet {
    CT("ct", ArmorForm.CT, "blue", null),
    T("t", ArmorForm.T, "beige", null),
    US_WOODLAND("us_woodland", ArmorForm.US, "woodland", CamoPattern.WOODLAND),
    US_DESERT("us_desert", ArmorForm.US, "sand", CamoPattern.DESERT),
    US_GREEN("us_green", ArmorForm.US, "olive", CamoPattern.OLIVE),
    US_MULTICAM("us_multicam", ArmorForm.US, "multicam", CamoPattern.MULTICAM),
    US_SNOW("us_snow", ArmorForm.US, "snow", CamoPattern.SNOW),
    RU_EMR("ru_emr", ArmorForm.RU, "emr", CamoPattern.EMR),
    GHILLIE("ghillie", ArmorForm.GHILLIE, "ghillie", CamoPattern.GHILLIE),
    SWAT("swat", ArmorForm.SWAT, "black", null);

    private final String id;
    private final ArmorForm form;
    private final String fabricColor;
    @Nullable
    private final CamoPattern camo;

    ArmorSet(String id, ArmorForm form, String fabricColor, @Nullable CamoPattern camo) {
        this.id = id;
        this.form = form;
        this.fabricColor = fabricColor;
        this.camo = camo;
    }

    public String id() {
        return id;
    }

    public ArmorForm form() {
        return form;
    }

    public String fabricId() {
        return fabricColor + "_fabric";
    }

    public boolean hasCamouflage() {
        return camo != null;
    }

    @Nullable
    public CamoPattern camo() {
        return camo;
    }

    public List<ArmorTier> tiers() {
        return this == GHILLIE ? List.of(ArmorTier.LIGHT) : List.of(ArmorTier.values());
    }
}
