package tacz_camo_armor.device;

import tacz_camo_armor.config.ServerConfig;

public enum DeviceKind {
    PVS14("nvg_pvs14", true),
    GPNVG18("nvg_gpnvg18", true),
    GPNVG18_GREEN("nvg_gpnvg18_green", true),
    THERMAL("nvg_thermal", true),
    GAS_MASK("gas_mask", false);

    private final String id;
    private final boolean nvg;

    DeviceKind(String id, boolean nvg) {
        this.id = id;
        this.nvg = nvg;
    }

    public String id() {
        return id;
    }

    public boolean isNvg() {
        return nvg;
    }

    /** NVG charge is in ticks, gas mask filter in seconds of blocked effects. */
    public int maxCharge() {
        return nvg ? ServerConfig.nvgBatterySeconds() * 20 : ServerConfig.gasFilterSeconds();
    }

    public boolean usesCharge() {
        return !nvg || ServerConfig.nvgUsesBattery();
    }
}
