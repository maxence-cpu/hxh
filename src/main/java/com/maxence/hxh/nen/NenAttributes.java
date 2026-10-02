package com.maxence.hxh.nen;

import com.maxence.hxh.HxHMod;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

/**
 * Traduit les statistiques de Nen (et le Ren) en modificateurs d'attributs vanilla.
 * Appelé à la connexion, à la réapparition, à chaque point dépensé et à chaque bascule de mode.
 */
public final class NenAttributes {
    private static final Identifier STRENGTH_DAMAGE = HxHMod.id("stat_strength_damage");
    private static final Identifier AGILITY_SPEED = HxHMod.id("stat_agility_speed");
    private static final Identifier ENDURANCE_HEALTH = HxHMod.id("stat_endurance_health");
    private static final Identifier REN_DAMAGE = HxHMod.id("ren_damage");
    private static final Identifier REN_SPEED = HxHMod.id("ren_speed");

    public static void apply(Player player, INenData data) {
        boolean on = data.isAwakened();
        int str = on ? data.getStat(NenStat.STRENGTH) : 0;
        int agi = on ? data.getStat(NenStat.AGILITY) : 0;
        int end = on ? data.getStat(NenStat.ENDURANCE) : 0;
        boolean ren = on && data.isModeActive(NenMode.REN);

        // Force : +0,5 dégât par point (x1,5 pour les Renforceurs, leur spécialité).
        double strBonus = str * 0.5D * (data.getCategory() == NenCategory.ENHANCER ? 1.5D : 1D);
        set(player, Attributes.ATTACK_DAMAGE, STRENGTH_DAMAGE, strBonus, AttributeModifier.Operation.ADD_VALUE);
        // Agilité : +1 % de vitesse par point.
        set(player, Attributes.MOVEMENT_SPEED, AGILITY_SPEED, agi * 0.01D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        // Endurance : +1 cœur par point.
        set(player, Attributes.MAX_HEALTH, ENDURANCE_HEALTH, end * 2D, AttributeModifier.Operation.ADD_VALUE);
        // Ren : +30 % dégâts, +10 % vitesse tant qu'il est actif.
        set(player, Attributes.ATTACK_DAMAGE, REN_DAMAGE, ren ? 0.30D : 0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        set(player, Attributes.MOVEMENT_SPEED, REN_SPEED, ren ? 0.10D : 0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void set(Player player, Holder<Attribute> attribute, Identifier id, double amount,
                            AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        instance.removeModifier(id);
        if (amount != 0D) {
            instance.addTransientModifier(new AttributeModifier(id, amount, operation));
        }
    }

    private NenAttributes() {}
}
