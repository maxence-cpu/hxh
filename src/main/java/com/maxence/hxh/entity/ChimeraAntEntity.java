package com.maxence.hxh.entity;

import com.maxence.hxh.nen.NenAttachments;
import com.maxence.hxh.nen.NenMode;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

import java.util.EnumSet;
import java.util.List;

/**
 * Fourmi-Chimère ouvrière.
 * - Rapide, bondit sur sa cible, chasse en groupe (une fourmi qui repère une proie alerte les autres).
 * - Sensible au Nen : ne détecte pas un joueur en Zetsu au-delà de 10 blocs.
 */
public class ChimeraAntEntity extends Monster {
    private static final double ZETSU_DETECTION_RANGE = 10D;
    private static final double SWARM_CALL_RANGE = 16D;

    public ChimeraAntEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 8;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.33D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 28.0D);
    }

    @Override
    protected void registerGoals() {
        // --- Comportement ---
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new CallSwarmGoal(this));
        this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.3D, true));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        // --- Ciblage ---
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(ChimeraAntEntity.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                (target, level) -> canSense(target)));
    }

    /** Un joueur en Zetsu est indétectable au-delà de 10 blocs. */
    public boolean canSense(LivingEntity target) {
        if (target instanceof Player player && this.distanceTo(player) > ZETSU_DETECTION_RANGE) {
            return NenAttachments.get(player)
                    .map(data -> !(data.isAwakened() && data.isModeActive(NenMode.ZETSU)))
                    .orElse(true);
        }
        return true;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        // Si la cible passe en Zetsu et s'éloigne, la fourmi perd sa trace.
        LivingEntity target = this.getTarget();
        if (target != null && this.tickCount % 10 == 0 && !canSense(target)) {
            this.setTarget(null);
        }
    }

    @Override protected SoundEvent getAmbientSound() { return SoundEvents.SILVERFISH_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.SPIDER_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.SPIDER_DEATH; }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SPIDER_STEP, 0.15F, 1.4F);
    }

    /**
     * Goal de poursuite en essaim : quand cette fourmi a une cible, elle "appelle"
     * les fourmis voisines sans cible pour qu'elles rejoignent la chasse.
     * Ne bloque aucun autre goal (aucun flag), il tourne en parallèle de l'attaque.
     */
    static class CallSwarmGoal extends Goal {
        private final ChimeraAntEntity ant;
        private int cooldown;

        CallSwarmGoal(ChimeraAntEntity ant) {
            this.ant = ant;
            this.setFlags(EnumSet.noneOf(Flag.class));
        }

        @Override
        public boolean canUse() {
            return ant.getTarget() != null && ant.getTarget().isAlive();
        }

        @Override
        public void start() {
            cooldown = 0;
        }

        @Override
        public void tick() {
            if (--cooldown > 0) return;
            cooldown = 40; // toutes les 2 secondes
            LivingEntity prey = ant.getTarget();
            if (prey == null) return;
            List<ChimeraAntEntity> allies = ant.level().getEntitiesOfClass(ChimeraAntEntity.class,
                    ant.getBoundingBox().inflate(SWARM_CALL_RANGE),
                    other -> other != ant && other.getTarget() == null && other.canSense(prey));
            for (ChimeraAntEntity ally : allies) {
                ally.setTarget(prey);
            }
        }
    }
}
