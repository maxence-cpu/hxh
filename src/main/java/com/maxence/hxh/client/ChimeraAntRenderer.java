package com.maxence.hxh.client;

import com.maxence.hxh.entity.ChimeraAntEntity;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

/**
 * Rendu provisoire de la Fourmi-Chimère : réutilise le modèle d'araignée vanilla
 * avec une texture propre au mod (assets/hxh/textures/entity/chimera_ant.png).
 * À remplacer par un modèle Blockbench dédié.
 */
public class ChimeraAntRenderer extends MobRenderer<ChimeraAntEntity, LivingEntityRenderState, SpiderModel> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("hxh", "textures/entity/chimera_ant.png");

    public ChimeraAntRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiderModel(context.bakeLayer(ModelLayers.SPIDER)), 0.8F);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }
}
