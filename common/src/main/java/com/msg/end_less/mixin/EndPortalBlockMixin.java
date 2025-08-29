/*
 * End-Less Mod
 * Copyright (c) 2025 MikeStorm03
 *
 * Licensed under the GNU General Public License v3.0
 * See the LICENSE file for details.
 */

package com.msg.end_less.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.levelgen.feature.EndPlatformFeature;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.msg.end_less.EndLessCommon;
import com.msg.end_less.features.EndLessSaveAndLoader;
import com.msg.end_less.features.NewEndPlatform;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin implements Portal {
    
    @Overwrite
    public TeleportTransition getPortalDestination(ServerLevel serverLevel, Entity entity, BlockPos blockPos) {
        ResourceKey<Level> dimension = serverLevel.dimension();

        if (shouldTeleportToEnd(serverLevel, entity)) {
            return teleportToEnd(serverLevel, entity);
        }

        if (dimension == EndLessCommon.NEW_END) {
            return teleportFromNewEndToOverworld(serverLevel, entity);
        }

        if (dimension == Level.OVERWORLD) {
            return teleportToNewEnd(serverLevel, entity);
        }

        return toSpawnPoint(serverLevel, entity);
    }

    private static boolean shouldTeleportToEnd(ServerLevel level, Entity entity) {
        EndDragonFight endDragonFight = level.getServer().getLevel(Level.END).getDragonFight();
        boolean dragonDead = (endDragonFight == null || !endDragonFight.hasPreviouslyKilledDragon());

        double x = entity.getX();
        double z = entity.getZ();
        boolean inSpawnArea = (x > -20 && x < 20 && z > -20 && z < 20 && level.dimension() == Level.OVERWORLD);

        return dragonDead || inSpawnArea;
    }

    private static TeleportTransition teleportToEnd(ServerLevel level, Entity entity) {
        ServerLevel endLevel = level.getServer().getLevel(Level.END);
        if (endLevel == null) return null;

        BlockPos spawn = ServerLevel.END_SPAWN_POINT;
        EndPlatformFeature.createEndPlatform(endLevel, spawn.below(), true);
        Vec3 pos = spawn.getBottomCenter();

        if (entity instanceof ServerPlayer) {
            pos = pos.subtract(0, 1, 0);
        }

        return transitionTo(endLevel, pos, entity, Direction.WEST.toYRot());
    }

    private static TeleportTransition teleportFromNewEndToOverworld(ServerLevel level, Entity entity) {
        level = level.getServer().getLevel(Level.OVERWORLD);
        if (level == null) return null;

        BlockPos nearest = findClosestPortalNear(entity, EndLessSaveAndLoader.getServerState(level.getServer()).openEndPortal, 5);
        if (nearest != null) {
            BlockPos safe = findSafeTeleportSpot(level, nearest);
            if (safe != null) {
                checkIfPortalDestroyed(level, nearest);
                return transitionTo(level, safe.getBottomCenter(), entity);
            }
            return transitionTo(level, randomTeleportySpot(nearest).getBottomCenter(), entity);
        }

        return toSpawnPoint(level, entity);
    }

    private static void checkIfPortalDestroyed(ServerLevel level, BlockPos blockPos) {
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (level.getBlockState(blockPos.offset(i, 0, j)).is(Blocks.END_PORTAL)) return;
            }
        }
        NewEndPlatform.placePortal(level, blockPos);
    }

    private static TeleportTransition teleportToNewEnd(ServerLevel level, Entity entity) {
        ServerLevel newEnd = level.getServer().getLevel(EndLessCommon.NEW_END);

        BlockPos nearest = findClosestPortalNear(entity, EndLessSaveAndLoader.getServerState(level.getServer()).openEndPortal, 5);
        if (nearest != null) {
            NewEndPlatform.createPlatform(newEnd, new BlockPos(nearest.getX(), 48, nearest.getZ()));
            return transitionTo(newEnd, new Vec3(entity.getX(), 55, entity.getZ()), entity);
        }

        NewEndPlatform.createPlatform(newEnd, new BlockPos(0, 48, 0));
        BlockPos fallback = entity.adjustSpawnLocation(newEnd, newEnd.getSharedSpawnPos());
        return transitionTo(newEnd, fallback.getBottomCenter(), entity);
    }

    private static BlockPos findClosestPortalNear(Entity entity, List<BlockPos> portals, double maxDistance) {
        double maxSqr = maxDistance * maxDistance;
        double x = entity.getX(), z = entity.getZ();

        for (BlockPos pos : portals) {
            double dx = pos.getX() - x;
            double dz = pos.getZ() - z;
            if ((dx * dx + dz * dz) < maxSqr) {
                return pos;
            }
        }

        return null;
    }

    private static BlockPos findSafeTeleportSpot(ServerLevel level, BlockPos center) {
        List<BlockPos> candidates = new ArrayList<>(15 * 15 * 10);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        int minY = center.getY() - 5;
        int maxY = center.getY() + 4;

        for (int m = -7; m <= 7; m++) {
            for (int n = -7; n <= 7; n++) {
                for (int o = minY; o <= maxY; o++) {
                    mutable.set(center.getX() + m, o, center.getZ() + n);
                    candidates.add(mutable.immutable()); // avoid reuse side effects
                }
            }
        }

        Collections.shuffle(candidates);

        for (BlockPos candidate : candidates) {
            if (isSafe(level, candidate)) {
                return candidate;
            }
        }

        return null;
    }

    private static BlockPos randomTeleportySpot(BlockPos center){
        BlockPos.MutableBlockPos mutable = center.mutable();
        Random random = new Random();
        int randomX = random.nextInt(5) - 2; // (max - min + 1) + min; max = 2; min = -2
        int randomZ = 2 == Math.abs(randomX) ? random.nextInt(3) - 1 : random.nextBoolean() ? 2 : -2; 
        return mutable.set(center).move(randomX, 1, randomZ);
    }

    private static TeleportTransition transitionTo(ServerLevel level, Vec3 pos, Entity entity) {
        return transitionTo(level, pos, entity, entity.getYRot());
    }

    private static TeleportTransition transitionTo(ServerLevel level, Vec3 pos, Entity entity, float yaw) {
        return new TeleportTransition(level, pos, entity.getDeltaMovement(), yaw, entity.getXRot(),
                TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET));
    }

    private static boolean isSafe(ServerLevel level, BlockPos blockPos) {
        BlockState below = level.getBlockState(blockPos.below());
        BlockState atBlock = level.getBlockState(blockPos);
        BlockState above = level.getBlockState(blockPos.above());

        boolean solidBase = below.isCollisionShapeFullBlock(level, blockPos);
        boolean passableTop = !above.isCollisionShapeFullBlock(level, blockPos) &&
                                above.getBlock() != Blocks.END_PORTAL &&
                                atBlock.getBlock() != Blocks.END_PORTAL;
        return solidBase && passableTop;
    }

    private static TeleportTransition toSpawnPoint(ServerLevel level, Entity entity) {
        if (entity instanceof ServerPlayer player) {
            return player.findRespawnPositionAndUseSpawnBlock(false, TeleportTransition.DO_NOTHING);
        }

        ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) return null;

        BlockPos fallback = entity.adjustSpawnLocation(overworld, overworld.getSharedSpawnPos());
        return transitionTo(overworld, fallback.getBottomCenter(), entity);
    }
}