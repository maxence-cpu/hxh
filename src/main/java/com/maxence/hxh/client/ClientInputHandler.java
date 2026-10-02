package com.maxence.hxh.client;

import com.maxence.hxh.ability.BigBangImpactAbility;
import com.maxence.hxh.ability.NenAbilities;
import com.maxence.hxh.nen.NenMode;
import com.maxence.hxh.network.ToggleModePayload;
import com.maxence.hxh.network.UseAbilityPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * Gestion des entrées côté client :
 * - touches H / R / Z / G -> ouverture du menu ou paquet de bascule de mode ;
 * - charge du Poing de la Destruction (clic droit maintenu main vide), envoyée au relâchement.
 */
public final class ClientInputHandler {
    private static int bigBangCharge;
    private static boolean charging;

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        while (ModKeyMappings.OPEN_NEN_MENU.consumeClick()) {
            if (mc.screen == null) mc.setScreen(new NenScreen());
        }
        while (ModKeyMappings.TOGGLE_REN.consumeClick()) ClientPacketDistributor.sendToServer(new ToggleModePayload(NenMode.REN));
        while (ModKeyMappings.TOGGLE_ZETSU.consumeClick()) ClientPacketDistributor.sendToServer(new ToggleModePayload(NenMode.ZETSU));
        while (ModKeyMappings.TOGGLE_EN.consumeClick()) ClientPacketDistributor.sendToServer(new ToggleModePayload(NenMode.EN));

        tickBigBangCharge(mc, player);
    }

    private static void tickBigBangCharge(Minecraft mc, LocalPlayer player) {
        boolean canCharge = mc.screen == null
                && ClientNenData.isAwakened()
                && ClientNenData.hasAbility(BigBangImpactAbility.ID)
                && !ClientNenData.isModeActive(NenMode.ZETSU)
                && player.getMainHandItem().isEmpty();
        boolean holding = canCharge && mc.options.keyUse.isDown();

        if (holding) {
            charging = true;
            int max = NenAbilities.BIG_BANG_IMPACT.getMaxCharge();
            if (bigBangCharge < max) bigBangCharge++;
            if (bigBangCharge % 5 == 0) {
                // petites particules de concentration autour du poing (purement visuel)
                player.level().addParticle(net.minecraft.core.particles.ParticleTypes.ENCHANT,
                        player.getX(), player.getY() + 1.2, player.getZ(), 0, 0.2, 0);
            }
        } else if (charging) {
            if (bigBangCharge >= NenAbilities.BIG_BANG_IMPACT.getMinCharge()) {
                ClientPacketDistributor.sendToServer(new UseAbilityPayload(BigBangImpactAbility.ID, bigBangCharge));
                player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
            }
            charging = false;
            bigBangCharge = 0;
        }
    }

    /** Ratio de charge (0 à 1) pour l'affichage dans le HUD. */
    public static float getChargeRatio() {
        return charging ? bigBangCharge / (float) NenAbilities.BIG_BANG_IMPACT.getMaxCharge() : 0F;
    }

    private ClientInputHandler() {}
}
