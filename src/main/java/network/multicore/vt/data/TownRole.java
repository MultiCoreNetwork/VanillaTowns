package network.multicore.vt.data;

public enum TownRole {
    MAYOR("mayor"),
    OFFICER("officer"),
    CITIZEN("citizen");

    private final String name;

    TownRole(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
