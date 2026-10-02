package com.maxence.hxh.nen;

import net.minecraft.nbt.CompoundTag;

import java.util.Set;

/**
 * Données de Nen attachées à chaque joueur (via la capability {@link NenAttachments#NEN}).
 * Toute la logique de règles (formules, coûts) est centralisée ici pour que serveur et
 * client calculent exactement les mêmes valeurs.
 */
public interface INenData {
    int POINTS_PER_LEVEL = 3;
    int MAX_LEVEL = 100;

    // --- Éveil ---
    boolean isAwakened();
    void setAwakened(boolean awakened);
    NenCategory getCategory();
    void setCategory(NenCategory category);

    // --- Progression ---
    int getLevel();
    int getXp();
    int getSkillPoints();
    void setSkillPoints(int points);
    /** Ajoute de l'XP de Nen ; renvoie le nombre de niveaux gagnés. */
    int addXp(int amount);

    static int xpForNextLevel(int level) {
        return 100 + 50 * (level - 1);
    }

    // --- Statistiques ---
    int getStat(NenStat stat);
    void setStat(NenStat stat, int value);

    // --- Aura ---
    float getAura();
    void setAura(float aura);

    default float getMaxAura() {
        return 100F + getStat(NenStat.MASTERY) * 20F;
    }

    /** Aura régénérée par seconde quand aucun mode coûteux n'est actif. */
    default float getAuraRegenPerSecond() {
        float regen = 2F + getStat(NenStat.MASTERY) * 0.15F;
        return isModeActive(NenMode.ZETSU) ? regen * 1.5F : regen;
    }

    /** L'Endurance réduit toutes les consommations d'Aura (jusqu'à -60 %). */
    default float getAuraCostMultiplier() {
        return 1F - Math.min(0.6F, getStat(NenStat.ENDURANCE) * 0.02F);
    }

    /** Rayon de l'En en blocs. */
    default double getEnRadius() {
        return Math.min(48D, 8D + getStat(NenStat.MASTERY) * 0.5D);
    }

    // --- Modes (Ren / Zetsu / En) ---
    boolean isModeActive(NenMode mode);
    void setModeActive(NenMode mode, boolean active);
    int getModeBits();
    void setModeBits(int bits);

    // --- Hatsu débloqués ---
    Set<String> getUnlockedAbilities();
    default boolean hasAbility(String id) { return getUnlockedAbilities().contains(id); }
    void unlockAbility(String id);

    // --- Cooldowns (non sauvegardés) ---
    long getCooldownEnd(String abilityId);
    void setCooldownEnd(String abilityId, long gameTime);

    // --- Synchronisation ---
    /** Changement important : synchroniser au prochain tick. */
    boolean isDirty();
    void setDirty(boolean dirty);
    /** Seule l'Aura a bougé : synchroniser périodiquement. */
    boolean isAuraDirty();
    void setAuraDirty(boolean dirty);

    // --- Persistance ---
    CompoundTag save();
    void load(CompoundTag tag);
    void copyFrom(INenData other);
}
