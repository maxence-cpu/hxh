package com.maxence.hxh.registry;

import com.maxence.hxh.HxHMod;
import com.maxence.hxh.entity.ChimeraAntEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, HxHMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<ChimeraAntEntity>> CHIMERA_ANT =
            ENTITIES.register("chimera_ant", id -> EntityType.Builder.of(ChimeraAntEntity::new, MobCategory.MONSTER)
                    .sized(1.0F, 0.9F)
                    .clientTrackingRange(8)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    private ModEntities() {}
}
