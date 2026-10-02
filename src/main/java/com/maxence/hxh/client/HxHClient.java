package com.maxence.hxh.client;

import com.maxence.hxh.HxHMod;
import com.maxence.hxh.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

/** Point d'entrée client : chargé uniquement sur le client (dist = CLIENT). */
@Mod(value = HxHMod.MODID, dist = Dist.CLIENT)
public final class HxHClient {

    public HxHClient(IEventBus modBus, ModContainer container) {
        // Mod bus
        modBus.addListener((RegisterKeyMappingsEvent e) -> ModKeyMappings.register(e));
        modBus.addListener((EntityRenderersEvent.RegisterRenderers e) ->
                e.registerEntityRenderer(ModEntities.CHIMERA_ANT.get(), ChimeraAntRenderer::new));
        modBus.addListener((RegisterGuiLayersEvent e) ->
                e.registerAbove(VanillaGuiLayers.FOOD_LEVEL, HxHMod.id("aura_bar"), AuraHud::render));

        // Bus de jeu
        NeoForge.EVENT_BUS.addListener(ClientInputHandler::onClientTick);
    }
}
