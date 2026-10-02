package com.maxence.hxh.network;

import com.maxence.hxh.HxHMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client → Serveur : déclencher un Hatsu.
 * {@code charge} = ticks de concentration côté client ; le serveur le borne
 * (AbstractNenAbility#clampCharge) pour qu'un client modifié ne puisse pas tricher.
 */
public record UseAbilityPayload(String abilityId, int charge) implements CustomPacketPayload {
    public static final Type<UseAbilityPayload> TYPE = new Type<>(HxHMod.id("use_ability"));
    public static final StreamCodec<FriendlyByteBuf, UseAbilityPayload> STREAM_CODEC = CustomPacketPayload.codec(
            (payload, buf) -> {
                buf.writeUtf(payload.abilityId(), 64);
                buf.writeVarInt(payload.charge());
            },
            buf -> new UseAbilityPayload(buf.readUtf(64), buf.readVarInt()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
