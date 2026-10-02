package com.maxence.hxh.ability;

import com.maxence.hxh.nen.INenData;
import com.maxence.hxh.nen.NenActions;
import com.maxence.hxh.nen.NenCategory;
import com.maxence.hxh.nen.NenMode;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;

/**
 * Base de tous les Hatsu. Pour créer un nouveau pouvoir :
 *  1. étendre cette classe et implémenter {@link #execute} ;
 *  2. l'enregistrer dans {@link NenAbilities}.
 *
 * {@link #tryUse} applique toutes les règles communes (éveil, déblocage, Zetsu,
 * cooldown, coût en Aura) avant d'appeler {@link #execute}.
 */
public abstract class AbstractNenAbility {
    private final String id;
    private final String displayName;
    private final String description;
    private final NenCategory category;
    private final int unlockLevel;
    private final int unlockCost;
    private final int cooldownTicks;

    protected AbstractNenAbility(String id, String displayName, String description, NenCategory category,
                                 int unlockLevel, int unlockCost, int cooldownTicks) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.category = category;
        this.unlockLevel = unlockLevel;
        this.unlockCost = unlockCost;
        this.cooldownTicks = cooldownTicks;
    }

    /** Coût brut en Aura (avant réduction par l'Endurance). */
    protected abstract float getBaseAuraCost(INenData data, int charge);

    /**
     * Effet du pouvoir, côté serveur.
     * @return true si le pouvoir s'est réellement déclenché (l'Aura et le cooldown ne sont consommés que dans ce cas)
     */
    protected abstract boolean execute(ServerPlayer player, INenData data, int charge);

    /** Bornes de la charge (en ticks). Par défaut : pas de charge. */
    public int getMinCharge() { return 0; }
    public int getMaxCharge() { return 0; }

    public final int clampCharge(int charge) {
        return Math.clamp(charge, getMinCharge(), getMaxCharge());
    }

    /** Un Spécialiste peut tout apprendre ; les autres seulement leur catégorie. */
    public boolean isAvailableFor(NenCategory playerCategory) {
        return playerCategory == category || playerCategory == NenCategory.SPECIALIST;
    }

    public final void tryUse(ServerPlayer player, INenData data, int rawCharge) {
        if (!data.isAwakened() || !data.hasAbility(id)) return;
        if (data.isModeActive(NenMode.ZETSU)) {
            NenActions.notify(player, "Impossible d'utiliser un Hatsu en Zetsu.", ChatFormatting.GRAY);
            return;
        }
        int charge = clampCharge(rawCharge);
        long now = player.level().getGameTime();
        long readyAt = data.getCooldownEnd(id);
        if (now < readyAt) {
            NenActions.notify(player, displayName + " : encore " + (readyAt - now + 19) / 20 + " s", ChatFormatting.GRAY);
            return;
        }
        float cost = getBaseAuraCost(data, charge) * data.getAuraCostMultiplier();
        if (data.getAura() < cost) {
            NenActions.notify(player, "Pas assez d'Aura (" + Math.round(cost) + " requis).", ChatFormatting.RED);
            return;
        }
        if (execute(player, data, charge)) {
            data.setAura(data.getAura() - cost);
            data.setCooldownEnd(id, now + cooldownTicks);
            data.setDirty(true);
        }
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public NenCategory getCategory() { return category; }
    public int getUnlockLevel() { return unlockLevel; }
    public int getUnlockCost() { return unlockCost; }
    public int getCooldownTicks() { return cooldownTicks; }
}
