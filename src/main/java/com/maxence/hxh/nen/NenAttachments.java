package com.maxence.hxh.nen;

import com.maxence.hxh.HxHMod;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Données de Nen attachées au joueur via le système "Data Attachments" de NeoForge
 * (l'équivalent moderne des Capabilities de Forge).
 * - sauvegardées avec le joueur grâce au codec ;
 * - recopiées automatiquement à la mort (copyOnDeath).
 */
public final class NenAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, HxHMod.MODID);

    /** Le NBT de NenData est enveloppé dans un codec pour la sauvegarde. */
    private static final Codec<NenData> CODEC = CompoundTag.CODEC.xmap(tag -> {
        NenData data = new NenData();
        data.load(tag);
        return data;
    }, NenData::save);

    public static final Supplier<AttachmentType<NenData>> NEN = ATTACHMENT_TYPES.register("nen",
            () -> AttachmentType.builder(NenData::new)
                    .serialize(CODEC.fieldOf("nen"))
                    .copyOnDeath()
                    .build());

    /** Accès pratique : un joueur a toujours des données (créées à la première lecture). */
    public static Optional<INenData> get(Player player) {
        return Optional.of(player.getData(NEN));
    }

    private NenAttachments() {}
}
