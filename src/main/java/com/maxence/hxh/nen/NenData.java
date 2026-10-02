package com.maxence.hxh.nen;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Implémentation par défaut de {@link INenData}. */
public class NenData implements INenData {
    private boolean awakened;
    private NenCategory category = NenCategory.NONE;
    private int level = 1;
    private int xp;
    private int skillPoints;
    private final int[] stats = new int[NenStat.values().length];
    private float aura;
    private int modeBits;
    private final Set<String> unlocked = new LinkedHashSet<>();
    private final Map<String, Long> cooldowns = new HashMap<>();
    private boolean dirty = true;
    private boolean auraDirty;

    // ---------------- Éveil ----------------
    @Override public boolean isAwakened() { return awakened; }
    @Override public void setAwakened(boolean awakened) { this.awakened = awakened; dirty = true; }
    @Override public NenCategory getCategory() { return category; }
    @Override public void setCategory(NenCategory category) { this.category = category; dirty = true; }

    // ---------------- Progression ----------------
    @Override public int getLevel() { return level; }
    @Override public int getXp() { return xp; }
    @Override public int getSkillPoints() { return skillPoints; }
    @Override public void setSkillPoints(int points) { this.skillPoints = Math.max(0, points); dirty = true; }

    @Override
    public int addXp(int amount) {
        if (amount <= 0 || level >= MAX_LEVEL) return 0;
        xp += amount;
        int gained = 0;
        while (level < MAX_LEVEL && xp >= INenData.xpForNextLevel(level)) {
            xp -= INenData.xpForNextLevel(level);
            level++;
            skillPoints += POINTS_PER_LEVEL;
            gained++;
        }
        if (level >= MAX_LEVEL) xp = 0;
        dirty = true;
        return gained;
    }

    // ---------------- Stats ----------------
    @Override public int getStat(NenStat stat) { return stats[stat.ordinal()]; }

    @Override
    public void setStat(NenStat stat, int value) {
        stats[stat.ordinal()] = Math.clamp(value, 0, NenStat.MAX_VALUE);
        if (stat == NenStat.MASTERY) aura = Math.min(aura, getMaxAura());
        dirty = true;
    }

    // ---------------- Aura ----------------
    @Override public float getAura() { return aura; }

    @Override
    public void setAura(float value) {
        float clamped = Math.clamp(value, 0F, getMaxAura());
        if (clamped != aura) {
            aura = clamped;
            auraDirty = true;
        }
    }

    // ---------------- Modes ----------------
    @Override public boolean isModeActive(NenMode mode) { return (modeBits & mode.bit()) != 0; }

    @Override
    public void setModeActive(NenMode mode, boolean active) {
        modeBits = active ? (modeBits | mode.bit()) : (modeBits & ~mode.bit());
        dirty = true;
    }

    @Override public int getModeBits() { return modeBits; }
    @Override public void setModeBits(int bits) { modeBits = bits; dirty = true; }

    // ---------------- Hatsu ----------------
    @Override public Set<String> getUnlockedAbilities() { return unlocked; }
    @Override public void unlockAbility(String id) { if (unlocked.add(id)) dirty = true; }

    // ---------------- Cooldowns ----------------
    @Override public long getCooldownEnd(String abilityId) { return cooldowns.getOrDefault(abilityId, 0L); }
    @Override public void setCooldownEnd(String abilityId, long gameTime) { cooldowns.put(abilityId, gameTime); }

    // ---------------- Sync ----------------
    @Override public boolean isDirty() { return dirty; }
    @Override public void setDirty(boolean dirty) { this.dirty = dirty; }
    @Override public boolean isAuraDirty() { return auraDirty; }
    @Override public void setAuraDirty(boolean dirty) { this.auraDirty = dirty; }

    // ---------------- NBT ----------------
    @Override
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("awakened", awakened);
        tag.putString("category", category.name());
        tag.putInt("level", level);
        tag.putInt("xp", xp);
        tag.putInt("points", skillPoints);
        for (NenStat stat : NenStat.values()) {
            tag.putInt("stat_" + stat.name().toLowerCase(), stats[stat.ordinal()]);
        }
        tag.putFloat("aura", aura);
        tag.putInt("modes", modeBits);
        tag.putString("abilities", String.join(",", unlocked));
        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        // Depuis MC 1.21.5, les getters NBT renvoient des Optional ; les variantes "...Or" donnent une valeur par défaut.
        awakened = tag.getBooleanOr("awakened", false);
        try {
            category = NenCategory.valueOf(tag.getStringOr("category", "NONE"));
        } catch (IllegalArgumentException e) {
            category = NenCategory.NONE;
        }
        level = Math.max(1, tag.getIntOr("level", 1));
        xp = tag.getIntOr("xp", 0);
        skillPoints = tag.getIntOr("points", 0);
        for (NenStat stat : NenStat.values()) {
            stats[stat.ordinal()] = tag.getIntOr("stat_" + stat.name().toLowerCase(), 0);
        }
        aura = tag.getFloatOr("aura", 0F);
        modeBits = tag.getIntOr("modes", 0);
        unlocked.clear();
        String abilities = tag.getStringOr("abilities", "");
        if (!abilities.isEmpty()) {
            for (String id : abilities.split(",")) unlocked.add(id);
        }
        dirty = true;
    }

    @Override
    public void copyFrom(INenData other) {
        load(other.save());
        // On ne garde pas les modes actifs après une mort.
        modeBits = 0;
    }
}
