package com.maxence.hxh.network;

import com.maxence.hxh.HxHMod;
import com.maxence.hxh.nen.NenStat;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client → Serveur : dépenser des points depuis l'interface (stat ou déblocage de Hatsu). */
public record SpendPointPayload(Action action, String target) implements CustomPacketPayload {
    public enum Action { STAT, UNLOCK }

    public static final Type<SpendPointPayload> TYPE = new Type<>(HxHMod.id("spend_point"));
    public static final StreamCodec<FriendlyByteBuf, SpendPointPayload> STREAM_CODEC = CustomPacketPayload.codec(
            (payload, buf) -> {
                buf.writeEnum(payload.action());
                buf.writeUtf(payload.target(), 64);
            },
            buf -> new SpendPointPayload(buf.readEnum(Action.class), buf.readUtf(64)));

    public static SpendPointPayload stat(NenStat stat) { return new SpendPointPayload(Action.STAT, stat.name()); }
    public static SpendPointPayload unlock(String abilityId) { return new SpendPointPayload(Action.UNLOCK, abilityId); }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
