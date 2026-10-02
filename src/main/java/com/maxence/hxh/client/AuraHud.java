package com.maxence.hxh.client;

import com.maxence.hxh.nen.NenMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.DeltaTracker;

/**
 * Couche de HUD (enregistrée au-dessus de la barre de faim) : barre d'Aura dessinée juste au-dessus de la barre de faim (côté droit de la hotbar).
 * La couleur reflète le mode actif : bleu (normal), doré (Ren), vert pâle (En), gris (Zetsu).
 * Une jauge blanche fine sous la barre montre la charge du Poing de la Destruction.
 */
public final class AuraHud {
    private static final int BAR_WIDTH = 81;   // même largeur que la barre de faim
    private static final int BAR_HEIGHT = 5;

    public static void render(GuiGraphicsExtractor g, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.isSpectator()) return;
        if (!ClientNenData.isAwakened()) return;

        int screenW = g.guiWidth();
        int screenH = g.guiHeight();

        // La barre de faim commence à (largeur/2 + 10) et se trouve à (hauteur - 39).
        int x = screenW / 2 + 10;
        int y = screenH - 39 - 11;
        if (mc.player.isUnderWater() || mc.player.getAirSupply() < mc.player.getMaxAirSupply()) {
            y -= 10; // laisser la place aux bulles d'air
        }

        float ratio = ClientNenData.getMaxAura() > 0 ? ClientNenData.getAura() / ClientNenData.getMaxAura() : 0F;
        int filled = Math.round((BAR_WIDTH - 2) * Math.clamp(ratio, 0F, 1F));

        int color;
        if (ClientNenData.isModeActive(NenMode.ZETSU)) color = 0xFF707070;
        else if (ClientNenData.isModeActive(NenMode.REN)) color = 0xFFFFC23A;
        else if (ClientNenData.isModeActive(NenMode.EN)) color = 0xFF9CF2B0;
        else color = 0xFF3FA9FF;

        g.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF101018);                 // contour
        g.fill(x + 1, y + 1, x + BAR_WIDTH - 1, y + BAR_HEIGHT - 1, 0xFF2A2A3A); // fond
        if (filled > 0) {
            g.fill(x + 1, y + 1, x + 1 + filled, y + BAR_HEIGHT - 1, color);
            g.fill(x + 1, y + 1, x + 1 + filled, y + 2, 0x55FFFFFF);             // reflet
        }

        float charge = ClientInputHandler.getChargeRatio();
        if (charge > 0F) {
            int cw = Math.round((BAR_WIDTH - 2) * charge);
            g.fill(x + 1, y + BAR_HEIGHT + 1, x + 1 + cw, y + BAR_HEIGHT + 3, 0xFFFFFFFF);
        }
    }

    private AuraHud() {}
}
