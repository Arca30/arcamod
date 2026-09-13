package dev.arca.arcamod.block.entity;

import dev.arca.arcamod.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class DisenchanterBlockEntity extends BlockEntity {
    public int ticks;
    public float nextPageAngle;
    public float pageAngle;
    public float flipRandom;
    public float flipTurn;
    public float nextPageTurningSpeed;
    public float pageTurningSpeed;
    public float bookRotation;
    public float lastBookRotation;
    public float targetBookRotation;
    private static final Random RANDOM = new Random();

    public DisenchanterBlockEntity(BlockPos pos, BlockState state) {
        // Assure-toi que "DISENCHANTER" correspond au nom de ta variable dans ModBlockEntities
        super(ModBlockEntities.DISENCHANTER, pos, state);
    }
    public static void bookAnimationTick(final Level level, final BlockPos worldPosition, final BlockState state, final EnchantingTableBlockEntity entity) {
        entity.oOpen = entity.open;
        entity.oRot = entity.rot;
        Player player = level.getNearestPlayer(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 3.0, false);
        if (player != null) {
            double xd = player.getX() - (worldPosition.getX() + 0.5);
            double zd = player.getZ() - (worldPosition.getZ() + 0.5);
            entity.tRot = (float) Mth.atan2(zd, xd);
            entity.open += 0.1F;
            if (entity.open < 0.5F || RANDOM.nextInt(40) == 0) {
                float old = entity.flipT;

                do {
                    entity.flipT = entity.flipT + (RANDOM.nextInt(4) - RANDOM.nextInt(4));
                } while (old == entity.flipT);
            }
        } else {
            entity.tRot += 0.02F;
            entity.open -= 0.1F;
        }

        while (entity.rot >= (float) Math.PI) {
            entity.rot -= (float) (Math.PI * 2);
        }

        while (entity.rot < (float) -Math.PI) {
            entity.rot += (float) (Math.PI * 2);
        }

        while (entity.tRot >= (float) Math.PI) {
            entity.tRot -= (float) (Math.PI * 2);
        }

        while (entity.tRot < (float) -Math.PI) {
            entity.tRot += (float) (Math.PI * 2);
        }

        float rotDir = entity.tRot - entity.rot;

        while (rotDir >= (float) Math.PI) {
            rotDir -= (float) (Math.PI * 2);
        }

        while (rotDir < (float) -Math.PI) {
            rotDir += (float) (Math.PI * 2);
        }

        entity.rot += rotDir * 0.4F;
        entity.open = Mth.clamp(entity.open, 0.0F, 1.0F);
        entity.time++;
        entity.oFlip = entity.flip;
        float diff = (entity.flipT - entity.flip) * 0.4F;
        float max = 0.2F;
        diff = Mth.clamp(diff, -0.2F, 0.2F);
        entity.flipA = entity.flipA + (diff - entity.flipA) * 0.9F;
        entity.flip = entity.flip + entity.flipA;
    }
}