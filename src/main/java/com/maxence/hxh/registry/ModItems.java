package com.maxence.hxh.registry;

import com.maxence.hxh.HxHMod;
import com.maxence.hxh.item.WaterLeafItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HxHMod.MODID);

    /** Feuille de l'Arbre à Eau : éveille les pores du joueur (catégorie de Nen aléatoire). */
    public static final DeferredItem<WaterLeafItem> WATER_LEAF = ITEMS.register("water_leaf",
            id -> new WaterLeafItem(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, id))
                    .stacksTo(16)
                    .rarity(Rarity.RARE)));

    private ModItems() {}
}
