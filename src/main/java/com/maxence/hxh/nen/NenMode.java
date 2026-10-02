package com.maxence.hxh.nen;

/** Modes passifs activables par touche. */
public enum NenMode {
    /** Ren : mode puissance, draine l'Aura en continu, booste les stats. */
    REN("Ren"),
    /** Zetsu : Aura annulée, invisible aux monstres sensibles au Nen au-delà de 10 blocs, dégâts physiques x2. */
    ZETSU("Zetsu"),
    /** En : détecte toutes les entités vivantes (Glowing) dans un rayon lié à la Maîtrise. */
    EN("En");

    private final String displayName;

    NenMode(String displayName) { this.displayName = displayName; }

    public String displayName() { return displayName; }

    public int bit() { return 1 << ordinal(); }

    public static NenMode byId(int id) {
        NenMode[] values = values();
        return id >= 0 && id < values.length ? values[id] : REN;
    }
}
