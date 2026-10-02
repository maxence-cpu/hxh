package com.maxence.hxh.nen;

import com.maxence.hxh.ability.AbstractNenAbility;
import com.maxence.hxh.ability.NenAbilities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Règles côté serveur pour les actions demandées par le client (via paquets).
 * Le serveur reste l'autorité : chaque demande est revérifiée ici.
 */
public final class NenActions {
    private static final float REN_MIN_AURA = 10F;
    private static final float EN_MIN_AURA = 20F;

    /** Active/désactive Ren, Zetsu ou En. */
    public static void toggleMode(ServerPlayer player, INenData data, NenMode mode) {
        if (!data.isAwakened()) {
            notify(player, "Tes pores ne sont pas encore ouverts au Nen.", ChatFormatting.GRAY);
            return;
        }
        boolean turningOn = !data.isModeActive(mode);

        if (turningOn) {
            switch (mode) {
                case REN -> {
                    if (data.getAura() < REN_MIN_AURA) { notify(player, "Pas assez d'Aura pour le Ren.", ChatFormatting.RED); return; }
                    data.setModeActive(NenMode.ZETSU, false);
                }
                case EN -> {
                    if (data.getAura() < EN_MIN_AURA) { notify(player, "Pas assez d'Aura pour l'En.", ChatFormatting.RED); return; }
                    data.setModeActive(NenMode.ZETSU, false);
                }
                case ZETSU -> {
                    // Le Zetsu coupe toute émission d'Aura.
                    data.setModeActive(NenMode.REN, false);
                    data.setModeActive(NenMode.EN, false);
                }
            }
        }
        data.setModeActive(mode, turningOn);
        NenAttributes.apply(player, data);
        notify(player, mode.displayName() + (turningOn ? " activé" : " désactivé"),
                turningOn ? ChatFormatting.AQUA : ChatFormatting.GRAY);
    }

    /** Dépense un point de compétence dans une statistique. */
    public static void spendStatPoint(ServerPlayer player, INenData data, NenStat stat) {
        if (!data.isAwakened() || data.getSkillPoints() <= 0) return;
        if (data.getStat(stat) >= NenStat.MAX_VALUE) return;
        data.setStat(stat, data.getStat(stat) + 1);
        data.setSkillPoints(data.getSkillPoints() - 1);
        NenAttributes.apply(player, data);
    }

    /** Débloque un Hatsu contre des points de compétence. */
    public static void unlockAbility(ServerPlayer player, INenData data, String abilityId) {
        AbstractNenAbility ability = NenAbilities.get(abilityId);
        if (ability == null || !data.isAwakened() || data.hasAbility(abilityId)) return;
        if (!ability.isAvailableFor(data.getCategory())) {
            notify(player, "Ce Hatsu ne correspond pas à ta catégorie de Nen.", ChatFormatting.RED);
            return;
        }
        if (data.getLevel() < ability.getUnlockLevel()) {
            notify(player, "Niveau de Nen " + ability.getUnlockLevel() + " requis.", ChatFormatting.RED);
            return;
        }
        if (data.getSkillPoints() < ability.getUnlockCost()) {
            notify(player, ability.getUnlockCost() + " points requis.", ChatFormatting.RED);
            return;
        }
        data.setSkillPoints(data.getSkillPoints() - ability.getUnlockCost());
        data.unlockAbility(abilityId);
        notify(player, "Hatsu débloqué : " + ability.getDisplayName(), ChatFormatting.GOLD);
    }

    public static void notify(ServerPlayer player, String message, ChatFormatting color) {
        player.sendOverlayMessage(Component.literal(message).withStyle(color));
    }

    private NenActions() {}
}
