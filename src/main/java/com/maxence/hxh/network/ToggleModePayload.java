package com.maxence.hxh.network;

import com.maxence.hxh.HxHMod;
import com.maxence.hxh.nen.NenMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client → Serveur : basculer Ren, Zetsu ou En. */
public record ToggleModePayload(NenMode mode) implements CustomPacketPayload {
    public static final Type<ToggleModePayload> TYPE = new Type<>(HxHMod.id("toggle_mode"));
    public static final StreamCodec<FriendlyByteBuf, ToggleModePayload> STREAM_CODEC = CustomPacketPayload.codec(
            (payload, buf) -> buf.writeVarInt(payload.mode().ordinal()),
            buf -> new ToggleModePayload(NenMode.byId(buf.readVarInt())));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
