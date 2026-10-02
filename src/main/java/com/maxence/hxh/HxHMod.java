package com.maxence.hxh;

import com.maxence.hxh.entity.ChimeraAntEntity;
import com.maxence.hxh.nen.NenAttachments;
import com.maxence.hxh.nen.NenEvents;
import com.maxence.hxh.network.ModNetwork;
import com.maxence.hxh.registry.ModEntities;
import com.maxence.hxh.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import org.slf4j.Logger;

/**
 * Point d'entrée commun (client + serveur) du mod Hunter x Hunter.
 * NeoForge injecte le bus du mod dans le constructeur ; les événements de jeu
 * passent par NeoForge.EVENT_BUS (voir {@link NenEvents}).
 * Le code purement client est dans {@link com.maxence.hxh.client.HxHClient}.
 */
@Mod(HxHMod.MODID)
public final class HxHMod {
    public static final String MODID = "hxh";
    public static final Logger LOGGER = LogUtils.getLogger();

    public HxHMod(IEventBus modBus, ModContainer container) {
        // Registres
        ModItems.ITEMS.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        NenAttachments.ATTACHMENT_TYPES.register(modBus);

        // Mod bus
        modBus.addListener(ModNetwork::register);
        modBus.addListener(this::registerAttributes);
        modBus.addListener(this::registerSpawnPlacements);

        // Bus de jeu
        NenEvents.register();
        LOGGER.info("[HxH] Le Nen s'éveille.");
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.CHIMERA_ANT.get(), ChimeraAntEntity.createAttributes().build());
    }

    private void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(ModEntities.CHIMERA_ANT.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    /** Raccourci pour créer un identifiant "hxh:chemin". */
    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
