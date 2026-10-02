package com.maxence.hxh.client;

import com.maxence.hxh.ability.AbstractNenAbility;
import com.maxence.hxh.ability.NenAbilities;
import com.maxence.hxh.nen.INenData;
import com.maxence.hxh.nen.NenMode;
import com.maxence.hxh.nen.NenStat;
import com.maxence.hxh.network.SpendPointPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Interface de progression (touche H) : niveau, XP, statistiques avec boutons "+",
 * et liste des Hatsu à débloquer. Chaque clic envoie un paquet au serveur, qui valide
 * puis renvoie l'état à jour (l'écran se met à jour tout seul via ClientNenData).
 */
public class NenScreen extends Screen {
    private static final int PANEL_W = 260;
    private static final int PANEL_H = 210;

    private final Map<NenStat, Button> statButtons = new EnumMap<>(NenStat.class);
    private final List<AbilityRow> abilityRows = new ArrayList<>();
    private int left;
    private int top;

    private record AbilityRow(AbstractNenAbility ability, Button button) {}

    public NenScreen() {
        super(Component.literal("Nen"));
    }

    @Override
    protected void init() {
        left = (width - PANEL_W) / 2;
        top = (height - PANEL_H) / 2;
        statButtons.clear();
        abilityRows.clear();

        int y = top + 62;
        for (NenStat stat : NenStat.values()) {
            Button plus = Button.builder(Component.literal("+"),
                            b -> ClientPacketDistributor.sendToServer(SpendPointPayload.stat(stat)))
                    .bounds(left + PANEL_W - 30, y - 4, 18, 16)
                    .tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(stat.description())))
                    .build();
            statButtons.put(stat, addRenderableWidget(plus));
            y += 20;
        }

        y = top + 160;
        for (AbstractNenAbility ability : NenAbilities.all()) {
            Button unlock = Button.builder(Component.literal("Débloquer"),
                            b -> ClientPacketDistributor.sendToServer(SpendPointPayload.unlock(ability.getId())))
                    .bounds(left + PANEL_W - 76, y - 4, 64, 16)
                    .tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(ability.getDescription())))
                    .build();
            abilityRows.add(new AbilityRow(ability, addRenderableWidget(unlock)));
            y += 20;
        }
        refreshButtons();
    }

    @Override
    public void tick() {
        super.tick();
        refreshButtons();
    }

    private void refreshButtons() {
        boolean awake = ClientNenData.isAwakened();
        for (Map.Entry<NenStat, Button> e : statButtons.entrySet()) {
            e.getValue().active = awake && ClientNenData.getSkillPoints() > 0
                    && ClientNenData.getStat(e.getKey()) < NenStat.MAX_VALUE;
        }
        for (AbilityRow row : abilityRows) {
            AbstractNenAbility a = row.ability();
            boolean owned = ClientNenData.hasAbility(a.getId());
            row.button().setMessage(Component.literal(owned ? "Acquis" : "Débloquer"));
            row.button().active = awake && !owned
                    && a.isAvailableFor(ClientNenData.getCategory())
                    && ClientNenData.getLevel() >= a.getUnlockLevel()
                    && ClientNenData.getSkillPoints() >= a.getUnlockCost();
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);

        int white = 0xFFFFFFFF, grey = 0xFFAAAAAA, gold = 0xFFFFC23A;
        g.fill(left, top, left + PANEL_W, top + PANEL_H, 0xE0101018);
        g.fill(left, top, left + PANEL_W, top + 1, ClientNenData.getCategory().color());

        g.drawString(font, "Nen", left + 10, top + 8, gold);

        if (!ClientNenData.isAwakened()) {
            g.drawString(font, "Tes pores ne sont pas encore ouverts.", left + 10, top + 30, grey);
            g.drawString(font, "Trouve une Feuille de l'Arbre à Eau...", left + 10, top + 44, grey);
            renderWidgetsOnTop(g, mouseX, mouseY, partialTick);
            return;
        }

        g.drawString(font, "Catégorie : " + ClientNenData.getCategory().displayName(),
                left + 10, top + 22, ClientNenData.getCategory().color());
        int level = ClientNenData.getLevel();
        String xpText = level >= INenData.MAX_LEVEL ? "MAX"
                : ClientNenData.getXp() + " / " + INenData.xpForNextLevel(level);
        g.drawString(font, "Niveau " + level + "   XP " + xpText, left + 10, top + 34, white);
        g.drawString(font, "Points : " + ClientNenData.getSkillPoints()
                + "   Aura : " + Math.round(ClientNenData.getAura()) + " / " + Math.round(ClientNenData.getMaxAura()),
                left + 10, top + 46, white);

        int y = top + 62;
        for (NenStat stat : NenStat.values()) {
            g.drawString(font, stat.displayName(), left + 10, y, white);
            g.drawString(font, String.valueOf(ClientNenData.getStat(stat)), left + PANEL_W - 60, y, gold);
            y += 20;
        }

        g.drawString(font, "Hatsu", left + 10, top + 146, gold);
        y = top + 160;
        for (AbilityRow row : abilityRows) {
            AbstractNenAbility a = row.ability();
            boolean compatible = a.isAvailableFor(ClientNenData.getCategory());
            g.drawString(font, a.getDisplayName(), left + 10, y, compatible ? white : grey);
            g.drawString(font, "Niv." + a.getUnlockLevel() + " · " + a.getUnlockCost() + " pts",
                    left + 10, y + 9, grey);
            y += 20;
        }

        StringBuilder modes = new StringBuilder();
        for (NenMode mode : NenMode.values()) {
            if (ClientNenData.isModeActive(mode)) modes.append(mode.displayName()).append(' ');
        }
        if (!modes.isEmpty()) g.drawString(font, "Actif : " + modes, left + PANEL_W - 110, top + 8, 0xFF9CF2B0);

        renderWidgetsOnTop(g, mouseX, mouseY, partialTick);
    }

    /** Le panneau est dessiné après super.render ; on redessine les boutons par-dessus. */
    private void renderWidgetsOnTop(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        for (Button b : statButtons.values()) b.render(g, mouseX, mouseY, partialTick);
        for (AbilityRow row : abilityRows) row.button().render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
