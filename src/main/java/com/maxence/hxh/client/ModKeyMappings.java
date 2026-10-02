package com.maxence.hxh.client;

import com.maxence.hxh.HxHMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/**
 * Touches du mod (modifiables dans Options > Commandes, catégorie "Hunter x Hunter").
 * Depuis MC 26.3 la gestion des entrées passe par SDL3 : on utilise les constantes
 * d'InputConstants plutôt que celles de GLFW.
 */
public final class ModKeyMappings {
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(HxHMod.id("hxh"));

    public static final KeyMapping OPEN_NEN_MENU = new KeyMapping("key.hxh.nen_menu",
            InputConstants.Type.KEYSYM, InputConstants.KEY_H, CATEGORY);
    public static final KeyMapping TOGGLE_REN = new KeyMapping("key.hxh.ren",
            InputConstants.Type.KEYSYM, InputConstants.KEY_R, CATEGORY);
    public static final KeyMapping TOGGLE_ZETSU = new KeyMapping("key.hxh.zetsu",
            InputConstants.Type.KEYSYM, InputConstants.KEY_Z, CATEGORY);
    public static final KeyMapping TOGGLE_EN = new KeyMapping("key.hxh.en",
            InputConstants.Type.KEYSYM, InputConstants.KEY_G, CATEGORY);

    public static void register(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(OPEN_NEN_MENU);
        event.register(TOGGLE_REN);
        event.register(TOGGLE_ZETSU);
        event.register(TOGGLE_EN);
    }

    private ModKeyMappings() {}
}
