package com.maxence.hxh.network;

import com.maxence.hxh.HxHMod;
import com.maxence.hxh.nen.INenData;
import com.maxence.hxh.nen.NenStat;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Serveur → Client : photo complète des données de Nen du joueur.
 * Envoyé à la connexion, à chaque changement important, et toutes les 10 ticks quand l'Aura bouge.
 */
public record SyncNenPayload(boolean awakened, int category, int level, int xp, int skillPoints, int[] stats,
                             float aura, float maxAura, int modeBits, String abilities) implements CustomPacketPayload {

    public static final Type<SyncNenPayload> TYPE = new Type<>(HxHMod.id("sync_nen"));
    public static final StreamCodec<FriendlyByteBuf, SyncNenPayload> STREAM_CODEC =
            CustomPacketPayload.codec(SyncNenPayload::write, SyncNenPayload::read);

    public SyncNenPayload(INenData data) {
        this(data.isAwakened(), data.getCategory().ordinal(), data.getLevel(), data.getXp(), data.getSkillPoints(),
                statsOf(data), data.getAura(), data.getMaxAura(), data.getModeBits(),
                String.join(",", data.getUnlockedAbilities()));
    }

    private static int[] statsOf(INenData data) {
        int[] stats = new int[NenStat.values().length];
        for (NenStat stat : NenStat.values()) stats[stat.ordinal()] = data.getStat(stat);
        return stats;
    }

    private static SyncNenPayload read(FriendlyByteBuf buf) {
        boolean awakened = buf.readBoolean();
        int category = buf.readVarInt();
        int level = buf.readVarInt();
        int xp = buf.readVarInt();
        int points = buf.readVarInt();
        int[] stats = new int[NenStat.values().length];
        for (int i = 0; i < stats.length; i++) stats[i] = buf.readVarInt();
        float aura = buf.readFloat();
        float maxAura = buf.readFloat();
        int modes = buf.readVarInt();
        String abilities = buf.readUtf(1024);
        return new SyncNenPayload(awakened, category, level, xp, points, stats, aura, maxAura, modes, abilities);
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeBoolean(awakened);
        buf.writeVarInt(category);
        buf.writeVarInt(level);
        buf.writeVarInt(xp);
        buf.writeVarInt(skillPoints);
        for (int stat : stats) buf.writeVarInt(stat);
        buf.writeFloat(aura);
        buf.writeFloat(maxAura);
        buf.writeVarInt(modeBits);
        buf.writeUtf(abilities, 1024);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
