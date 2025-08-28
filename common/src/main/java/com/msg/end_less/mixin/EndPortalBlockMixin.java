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
public class EndPortalBlockMixin {
    
    /**
     * Determines the appropriate portal destination based on entity and level state.
     *
     * @param level  The current server level.
     * @param entity The entity being teleported.
     * @param pos    The position triggering teleport (unused).
     * @return A DimensionTransition instance representing the teleportation result.
     */
    @Overwrite
    public TeleportTransition getPortalDestination(ServerLevel level, Entity entity, BlockPos pos) {
        ResourceKey<Level> dimension = level.dimension();

        if (shouldTeleportToEnd(level, entity)) {
            return teleportToEnd(level, entity);
        }

        if (dimension == EndLessCommon.NEW_END) {
            return teleportFromNewEndToOverworld(level, entity);
        }

        if (dimension == Level.OVERWORLD) {
            return teleportToNewEnd(level, entity);
        }

        return toSpawnPoint(level, entity);
    }

    /**
     * Checks whether the entity should be sent to the End dimension.
     *
     * @param level  The current server world.
     * @param entity The entity being teleported.
     * @return True if the dragon havent been killed or jump in the portal at (0;0) Overworld.
     */
    private static boolean shouldTeleportToEnd(ServerLevel level, Entity entity) {
        EndDragonFight endDragonFight = level.getServer().getLevel(Level.END).getDragonFight();
        boolean dragonDead = (endDragonFight == null || !endDragonFight.hasPreviouslyKilledDragon());

        double x = entity.getX();
        double z = entity.getZ();
        boolean inSpawnArea = (x > -20 && x < 20 && z > -20 && z < 20 && level.dimension() == Level.OVERWORLD);

        return dragonDead || inSpawnArea;
    }

    /**
     * Teleports the entity to the End and spawn platform.
     *
     * @param level  The current server world.
     * @param entity The entity being teleported.
     * @return A DimensionTransition to the End.
     */
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

    /**
     * Handles teleportation from the custom End dimension back to the Overworld.
     * <p>
     * Attempts to locate the nearest open portal stored in persistent server state.
     * If a portal is found, it will try to find a safe spot near it.
     * If no safe spot is available, it picks a randomized nearby location.
     * If no portal exists, the entity will be sent to the Overworld spawn point.
     *
     * @param level  The custom End dimension.
     * @param entity The entity to teleport.
     * @return A {@link DimensionTransition} representing the teleportation action.
     */
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

    /**
     * Handles teleportation from Overworld to NEW_END.
     *
     * @param level  The Overworld.
     * @param entity The entity being teleported.
     * @return A DimensionTransition to the NEW_END.
     */
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

    /**
     * Finds the closest portal within a max distance.
     *
     * @param entity       The entity position to compare.
     * @param portals      A list of portal positions.
     * @param maxDistance  The max distance to consider.
     * @return The closest portal position, or null if none is close enough.
     */
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

    /**
     * Attempts to find a safe location near a given center position to teleport an entity to.
     * <p>
     * The method scans a 15x15 area horizontally and a 10-block range vertically around the
     * specified {@code center} position. Each possible location is checked randomly to
     * improve fairness and reduce predictable patterns.
     * <p>
     * A location is considered safe if it passes the {@code isSafe} check (not defined here).
     *
     * @param level  The server level in which the search is performed.
     * @param center The center {@link BlockPos} around which to search for a safe teleport location.
     * @return A {@link BlockPos} representing a safe teleport location, or {@code null} if none is found.
     */
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

    /**
     * Generates a small random offset position near the specified center, avoiding direct center.
     * <p>
     * This is used as a fallback when no safe position is found via scanning.
     * The logic prefers positions 2 blocks away in X or Z to avoid tight overlaps.
     *
     * @param center The central position to offset from.
     * @return A nearby {@link BlockPos} within a small, predefined range.
     */
    private static BlockPos randomTeleportySpot(BlockPos center){
        BlockPos.MutableBlockPos mutable = center.mutable();
        Random random = new Random();
        int randomX = random.nextInt(5) - 2; // (max - min + 1) + min; max = 2; min = -2
        int randomZ = 2 == Math.abs(randomX) ? random.nextInt(3) - 1 : random.nextBoolean() ? 2 : -2; 
        return mutable.set(center).move(randomX, 1, randomZ);
    }

    /**
     * Creates a DimensionTransition at a given position with the entity's movement and rotation.
     *
     * @param level  The target level.
     * @param pos    The position to teleport to.
     * @param entity The entity being teleported.
     * @return A DimensionTransition to the specified location.
     */
    private static TeleportTransition transitionTo(ServerLevel level, Vec3 pos, Entity entity) {
        return transitionTo(level, pos, entity, entity.getYRot());
    }

    private static TeleportTransition transitionTo(ServerLevel level, Vec3 pos, Entity entity, float yaw) {
        return new TeleportTransition(level, pos, entity.getDeltaMovement(), yaw, entity.getXRot(),
                TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET));
    }

    /**
     * Determines whether a given block position is safe for teleportation.
     * <p>
     * A position is considered safe if:
     * <ul>
     *   <li>The block below has a full collision shape (i.e., a solid block to stand on).</li>
     *   <li>The block above does not have a full collision shape (i.e., won't suffocate the entity).</li>
     *   <li>Neither the current block nor the block above is an {@link net.minecraft.world.level.block.Blocks#END_PORTAL} block.</li>
     * </ul>
     * This method is useful for checking whether an entity can safely teleport to or spawn at a given location.
     *
     * @param level     The {@link ServerLevel} where the position is being checked.
     * @param blockPos  The {@link BlockPos} to evaluate for safety.
     * @return {@code true} if the position is safe, {@code false} otherwise.
     */
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

    /**
     * Returns a fallback spawn location transition when no other teleport options apply.
     *
     * @param level  The current server level.
     * @param entity The entity being teleported.
     * @return A DimensionTransition to the fallback point.
     */
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