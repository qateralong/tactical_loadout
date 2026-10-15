package tactical_loadout.item;

public enum ArmorForm {
    CT("ct"),
    T("t"),
    US("us"),
    RU("ru"),
    GHILLIE("ghillie"),
    SWAT("swat"),
    EOD("eod");

    private final String id;

    ArmorForm(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
