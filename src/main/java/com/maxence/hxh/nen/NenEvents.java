package com.maxence.hxh.nen;

import com.maxence.hxh.entity.ChimeraAntEntity;
import com.maxence.hxh.network.ModNetwork;
import com.maxence.hxh.network.SyncNenPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Tous les événements serveur liés au Nen :
 * régénération/drain d'Aura, effets des modes (Ren, Zetsu, En), bonus de stats,
 * gain d'XP, synchronisation client. (La sauvegarde et la copie à la mort sont gérées
 * par l'attachment, voir {@link NenAttachments}.)
 */
public final class NenEvents {
    private static final int XP_PER_CHIMERA_ANT = 25;
    private static final int AURA_SYNC_INTERVAL = 10; // ticks
    private static final double ZETSU_DAMAGE_MULTIPLIER = 2.0D;

    public static void register() {
        var bus = NeoForge.EVENT_BUS;
        bus.addListener((PlayerEvent.PlayerLoggedInEvent e) -> refresh(e.getEntity()));
        bus.addListener(NenEvents::onRespawn);
        bus.addListener((PlayerEvent.PlayerChangedDimensionEvent e) -> refresh(e.getEntity()));
        bus.addListener(NenEvents::onBreakSpeed);
        bus.addListener(NenEvents::onPlayerTick);
        bus.addListener(NenEvents::onIncomingDamage);
        bus.addListener(NenEvents::onLivingFall);
        bus.addListener(NenEvents::onLivingDeath);
    }

    /** Après une mort, les données sont copiées (copyOnDeath) mais on coupe les modes actifs. */
    private static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        NenAttachments.get(event.getEntity()).ifPresent(data -> data.setModeBits(0));
        refresh(event.getEntity());
    }

    /** Réapplique les attributs et force une synchro complète vers le client. */
    private static void refresh(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            NenAttachments.get(serverPlayer).ifPresent(data -> {
                NenAttributes.apply(serverPlayer, data);
                sync(serverPlayer, data);
            });
        }
    }

    public static void sync(ServerPlayer player, INenData data) {
        ModNetwork.sendToPlayer(player, new SyncNenPayload(data));
        data.setDirty(false);
        data.setAuraDirty(false);
    }

    // ------------------------------------------------------------------
    // Tick joueur : Aura, Ren, En, synchronisation
    // ------------------------------------------------------------------

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        NenAttachments.get(player).ifPresent(data -> {
            if (data.isAwakened()) {
                tickAura(player, data);
                if (data.isModeActive(NenMode.EN) && player.tickCount % 10 == 0) {
                    tickEn(player, data);
                }
            }
            if (data.isDirty() || (data.isAuraDirty() && player.tickCount % AURA_SYNC_INTERVAL == 0)) {
                sync(player, data);
            }
        });
    }

    private static void tickAura(ServerPlayer player, INenData data) {
        float costMult = data.getAuraCostMultiplier();
        float drainPerSecond = 0F;
        if (data.isModeActive(NenMode.REN)) drainPerSecond += 3F * costMult;
        if (data.isModeActive(NenMode.EN)) drainPerSecond += 10F * costMult;

        if (drainPerSecond > 0F) {
            data.setAura(data.getAura() - drainPerSecond / 20F);
            if (data.getAura() <= 0F) {
                data.setModeActive(NenMode.REN, false);
                data.setModeActive(NenMode.EN, false);
                NenAttributes.apply(player, data);
                NenActions.notify(player, "Ton Aura est épuisée !", ChatFormatting.RED);
            }
        } else {
            data.setAura(data.getAura() + data.getAuraRegenPerSecond() / 20F);
        }
    }

    /** En : révèle (Glowing) les entités vivantes dans le rayon. */
    private static void tickEn(ServerPlayer player, INenData data) {
        double r = data.getEnRadius();
        ServerLevel level = (ServerLevel) player.level();
        AABB area = player.getBoundingBox().inflate(r);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive() && e.distanceToSqr(player) <= r * r)) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 15, 0, false, false));
        }
    }

    // ------------------------------------------------------------------
    // Combat et déplacements
    // ------------------------------------------------------------------

    /** Zetsu : dégâts physiques (infligés par une entité) doublés. */
    private static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.getSource().getEntity() == null) return;
        NenAttachments.get(player).ifPresent(data -> {
            if (data.isAwakened() && data.isModeActive(NenMode.ZETSU)) {
                event.setAmount((float) (event.getAmount() * ZETSU_DAMAGE_MULTIPLIER));
            }
        });
    }

    /** Agilité : jusqu'à -60 % de dégâts de chute. */
    private static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        NenAttachments.get(player).ifPresent(data -> {
            if (!data.isAwakened()) return;
            float reduction = Math.min(0.6F, data.getStat(NenStat.AGILITY) * 0.02F);
            event.setDamageMultiplier(event.getDamageMultiplier() * (1F - reduction));
        });
    }

    /** Force : +4 % de vitesse de minage par point (+30 % supplémentaires en Ren). */
    private static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        NenAttachments.get(event.getEntity()).ifPresent(data -> {
            if (!data.isAwakened()) return;
            float mult = 1F + data.getStat(NenStat.STRENGTH) * 0.04F;
            if (data.isModeActive(NenMode.REN)) mult *= 1.3F;
            event.setNewSpeed(event.getNewSpeed() * mult);
        });
    }

    /** XP de Nen en tuant les mobs du mod. */
    private static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ChimeraAntEntity)) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        NenAttachments.get(player).ifPresent(data -> {
            if (!data.isAwakened()) return;
            int levels = data.addXp(XP_PER_CHIMERA_ANT);
            if (levels > 0) {
                NenActions.notify(player, "Niveau de Nen " + data.getLevel() + " ! +"
                        + levels * INenData.POINTS_PER_LEVEL + " points (touche H)", ChatFormatting.GOLD);
            }
        });
    }

    private NenEvents() {}
}
