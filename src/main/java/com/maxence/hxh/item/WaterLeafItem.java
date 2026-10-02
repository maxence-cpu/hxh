package com.maxence.hxh.item;

import com.maxence.hxh.nen.NenActions;
import com.maxence.hxh.nen.NenAttributes;
import com.maxence.hxh.nen.NenAttachments;
import com.maxence.hxh.nen.NenCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Feuille de l'Arbre à Eau : rappel du test de la coupe d'eau (Mizumishiki).
 * Clic droit : ouvre les pores du joueur et lui attribue une catégorie de Nen aléatoire.
 */
public class WaterLeafItem extends Item {
    private static final float STARTING_AURA = 50F;

    public WaterLeafItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        return NenAttachments.get(serverPlayer).map(data -> {
            if (data.isAwakened()) {
                NenActions.notify(serverPlayer, "Ton Nen est déjà éveillé : " + data.getCategory().displayName(),
                        ChatFormatting.GRAY);
                return InteractionResult.FAIL;
            }
            NenCategory category = NenCategory.random(level.getRandom());
            data.setAwakened(true);
            data.setCategory(category);
            data.setAura(STARTING_AURA);
            NenAttributes.apply(serverPlayer, data);

            serverPlayer.sendSystemMessage(Component.literal("Tes pores s'ouvrent... Catégorie de Nen : ")
                    .withStyle(ChatFormatting.WHITE)
                    .append(Component.literal(category.displayName()).withColor(category.color() & 0xFFFFFF)));

            ServerLevel serverLevel = (ServerLevel) level;
            serverLevel.sendParticles(ParticleTypes.SPLASH, player.getX(), player.getY() + 1, player.getZ(),
                    60, 0.5, 0.8, 0.5, 0.1);
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE,
                    SoundSource.PLAYERS, 1F, 1.2F);

            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.CONSUME;
        }).orElse(InteractionResult.PASS);
    }
}
