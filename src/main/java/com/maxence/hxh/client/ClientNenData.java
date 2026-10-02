package com.maxence.hxh.client;

import com.maxence.hxh.nen.NenCategory;
import com.maxence.hxh.nen.NenMode;
import com.maxence.hxh.nen.NenStat;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Copie côté client des données de Nen, remplie par {@code SyncNenPayload}.
 * Uniquement des données (aucune classe Minecraft client), pour rester sûr sur serveur dédié.
 * Le HUD et l'interface lisent ces valeurs ; elles ne font jamais autorité.
 */
public final class ClientNenData {
    private static boolean awakened;
    private static NenCategory category = NenCategory.NONE;
    private static int level = 1;
    private static int xp;
    private static int skillPoints;
    private static final int[] stats = new int[NenStat.values().length];
    private static float aura;
    private static float maxAura = 100F;
    private static int modeBits;
    private static Set<String> abilities = Collections.emptySet();

    public static void update(boolean awakened, int category, int level, int xp, int skillPoints, int[] stats,
                              float aura, float maxAura, int modeBits, String abilities) {
        ClientNenData.awakened = awakened;
        ClientNenData.category = NenCategory.byId(category);
        ClientNenData.level = level;
        ClientNenData.xp = xp;
        ClientNenData.skillPoints = skillPoints;
        System.arraycopy(stats, 0, ClientNenData.stats, 0, Math.min(stats.length, ClientNenData.stats.length));
        ClientNenData.aura = aura;
        ClientNenData.maxAura = maxAura;
        ClientNenData.modeBits = modeBits;
        ClientNenData.abilities = abilities.isEmpty()
                ? Collections.emptySet()
                : new LinkedHashSet<>(Arrays.asList(abilities.split(",")));
    }

    public static boolean isAwakened() { return awakened; }
    public static NenCategory getCategory() { return category; }
    public static int getLevel() { return level; }
    public static int getXp() { return xp; }
    public static int getSkillPoints() { return skillPoints; }
    public static int getStat(NenStat stat) { return stats[stat.ordinal()]; }
    public static float getAura() { return aura; }
    public static float getMaxAura() { return maxAura; }
    public static boolean isModeActive(NenMode mode) { return (modeBits & mode.bit()) != 0; }
    public static boolean hasAbility(String id) { return abilities.contains(id); }

    private ClientNenData() {}
}
