package com.maxence.hxh.network;

import com.maxence.hxh.client.ClientNenData;
import com.maxence.hxh.nen.NenActions;
import com.maxence.hxh.nen.NenAttachments;
import com.maxence.hxh.nen.NenStat;
import com.maxence.hxh.ability.AbstractNenAbility;
import com.maxence.hxh.ability.NenAbilities;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Enregistrement des paquets réseau ("payloads").
 *
 * Client → Serveur : demandes (basculer un mode, dépenser un point, utiliser un Hatsu).
 * Serveur → Client : état complet du Nen. Le serveur reste l'autorité et revérifie tout.
 *
 * Incrémenter PROTOCOL_VERSION à chaque changement de format d'un paquet.
 * Les handlers s'exécutent sur le thread principal du jeu.
 */
public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        // Serveur -> Client
        registrar.playToClient(SyncNenPayload.TYPE, SyncNenPayload.STREAM_CODEC, ModNetwork::handleSync);

        // Client -> Serveur
        registrar.playToServer(ToggleModePayload.TYPE, ToggleModePayload.STREAM_CODEC, ModNetwork::handleToggle);
        registrar.playToServer(SpendPointPayload.TYPE, SpendPointPayload.STREAM_CODEC, ModNetwork::handleSpend);
        registrar.playToServer(UseAbilityPayload.TYPE, UseAbilityPayload.STREAM_CODEC, ModNetwork::handleUseAbility);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    // ------------------------------------------------------------------ handlers

    /** ClientNenData ne contient que des données (aucune classe client) : sûr sur serveur dédié. */
    private static void handleSync(SyncNenPayload p, IPayloadContext ctx) {
        ClientNenData.update(p.awakened(), p.category(), p.level(), p.xp(), p.skillPoints(), p.stats(),
                p.aura(), p.maxAura(), p.modeBits(), p.abilities());
    }

    private static void handleToggle(ToggleModePayload p, IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer player) {
            NenAttachments.get(player).ifPresent(data -> NenActions.toggleMode(player, data, p.mode()));
        }
    }

    private static void handleSpend(SpendPointPayload p, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;
        NenAttachments.get(player).ifPresent(data -> {
            switch (p.action()) {
                case STAT -> {
                    try {
                        NenActions.spendStatPoint(player, data, NenStat.valueOf(p.target()));
                    } catch (IllegalArgumentException ignored) {
                        // paquet invalide (client modifié) : ignoré
                    }
                }
                case UNLOCK -> NenActions.unlockAbility(player, data, p.target());
            }
        });
    }

    private static void handleUseAbility(UseAbilityPayload p, IPayloadContext ctx) {
        AbstractNenAbility ability = NenAbilities.get(p.abilityId());
        if (ability != null && ctx.player() instanceof ServerPlayer player) {
            NenAttachments.get(player).ifPresent(data -> ability.tryUse(player, data, p.charge()));
        }
    }

    private ModNetwork() {}
}
