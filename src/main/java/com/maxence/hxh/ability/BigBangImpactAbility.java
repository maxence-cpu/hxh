package com.maxence.hxh.ability;

import com.maxence.hxh.nen.INenData;
import com.maxence.hxh.nen.NenActions;
import com.maxence.hxh.nen.NenCategory;
import com.maxence.hxh.nen.NenMode;
import com.maxence.hxh.nen.NenStat;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Renforcement — Poing de la Destruction.
 *
 * Côté client : maintenir le clic droit main vide concentre l'Aura (10 à 60 ticks).
 * Au relâchement, le client envoie un {@code UseAbilityPacket} avec la durée de charge.
 * Côté serveur (ici) : frappe l'entité visée à 4,5 blocs maximum, inflige de gros dégâts,
 * provoque une onde de choc visuelle et sonore SANS détruire de blocs, et touche
 * légèrement les entités autour du point d'impact.
 */
public class BigBangImpactAbility extends AbstractNenAbility {
    public static final String ID = "big_bang_impact";
    private static final double REACH = 4.5D;
    private static final double SPLASH_RADIUS = 2.5D;

    public BigBangImpactAbility() {
        super(ID, "Poing de la Destruction",
                "Maintiens clic droit main vide pour concentrer l'Aura, relâche pour frapper.",
                NenCategory.ENHANCER, 3, 5, 60);
    }

    @Override public int getMinCharge() { return 10; }
    @Override public int getMaxCharge() { return 60; }

    @Override
    protected float getBaseAuraCost(INenData data, int charge) {
        return 20F + charge; // 30 à 80 Aura
    }

    @Override
    protected boolean execute(ServerPlayer player, INenData data, int charge) {
        ServerLevel level = (ServerLevel) player.level();
        LivingEntity target = findTarget(player);
        if (target == null) {
            NenActions.notify(player, "Aucune cible à portée.", ChatFormatting.GRAY);
            return false;
        }

        float chargeRatio = charge / (float) getMaxCharge(); // 0,17 à 1
        float damage = (6F
                + data.getStat(NenStat.STRENGTH) * 0.6F
                + data.getStat(NenStat.MASTERY) * 0.4F) * (0.5F + 1.5F * chargeRatio);
        if (data.isModeActive(NenMode.REN)) damage *= 1.5F;

        // Coup principal
        target.hurtServer(level, player.damageSources().playerAttack(player), damage);
        Vec3 push = target.position().subtract(player.position()).normalize();
        double strength = 1.0D + 2.0D * chargeRatio;
        target.setDeltaMovement(target.getDeltaMovement().add(push.x * strength, 0.4D + 0.3D * chargeRatio, push.z * strength));
        target.hurtMarked = true; // force l'envoi de la nouvelle vitesse au client

        // Onde de choc : dégâts réduits autour de l'impact, aucun bloc détruit
        Vec3 impact = target.position().add(0, target.getBbHeight() / 2, 0);
        AABB area = new AABB(impact, impact).inflate(SPLASH_RADIUS);
        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e != target && e.isAlive())) {
            nearby.hurtServer(level, player.damageSources().playerAttack(player), damage * 0.3F);
        }

        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, impact.x, impact.y, impact.z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.END_ROD, impact.x, impact.y, impact.z, 40, 0.6, 0.6, 0.6, 0.25);
        level.playSound(null, impact.x, impact.y, impact.z, SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.PLAYERS, 1.2F, 0.8F + level.getRandom().nextFloat() * 0.2F);
        return true;
    }

    /** Lancer de rayon depuis les yeux du joueur pour trouver l'entité visée. */
    private static LivingEntity findTarget(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1F).scale(REACH));
        AABB searchBox = player.getBoundingBox().expandTowards(player.getViewVector(1F).scale(REACH)).inflate(1D);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, eye, end, searchBox,
                e -> e instanceof LivingEntity && e.isAlive() && !e.isSpectator() && e.isPickable(),
                REACH * REACH);
        if (hit == null) return null;
        Entity entity = hit.getEntity();
        return entity instanceof LivingEntity living ? living : null;
    }
}
