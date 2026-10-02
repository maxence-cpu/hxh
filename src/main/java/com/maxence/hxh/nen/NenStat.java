package com.maxence.hxh.nen;

/** Les quatre statistiques principales du joueur. */
public enum NenStat {
    STRENGTH("Force", "Dégâts de mêlée et vitesse de minage"),
    AGILITY("Agilité", "Vitesse de déplacement, dégâts de chute réduits"),
    ENDURANCE("Endurance", "Vie maximale, consommation d'Aura réduite"),
    MASTERY("Maîtrise du Nen", "Aura maximale, puissance des Hatsu");

    public static final int MAX_VALUE = 100;

    private final String displayName;
    private final String description;

    NenStat(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String displayName() { return displayName; }
    public String description() { return description; }

    public static NenStat byId(int id) {
        NenStat[] values = values();
        return id >= 0 && id < values.length ? values[id] : STRENGTH;
    }
}
