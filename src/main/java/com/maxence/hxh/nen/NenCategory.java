package com.maxence.hxh.nen;

import net.minecraft.util.RandomSource;

/** Les six catégories de Nen (diagramme hexagonal). */
public enum NenCategory {
    NONE("Aucune", 0xFFAAAAAA),
    ENHANCER("Renforcement", 0xFFE0C040),
    EMITTER("Émission", 0xFF40A0FF),
    CONJURER("Matérialisation", 0xFFB060FF),
    TRANSMUTER("Transformation", 0xFF60E0FF),
    MANIPULATOR("Manipulation", 0xFF60D060),
    SPECIALIST("Spécialisation", 0xFFFF5050);

    private final String displayName;
    private final int color;

    NenCategory(String displayName, int color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String displayName() { return displayName; }
    public int color() { return color; }

    /** Tirage à l'éveil : 2 % de Spécialistes, le reste équiréparti. */
    public static NenCategory random(RandomSource random) {
        if (random.nextFloat() < 0.02F) return SPECIALIST;
        NenCategory[] common = {ENHANCER, EMITTER, CONJURER, TRANSMUTER, MANIPULATOR};
        return common[random.nextInt(common.length)];
    }

    public static NenCategory byId(int id) {
        NenCategory[] values = values();
        return id >= 0 && id < values.length ? values[id] : NONE;
    }
}
