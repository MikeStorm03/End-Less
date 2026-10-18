/*
 * End-Less Mod
 * Copyright (c) 2025 MikeStorm03
 *
 * Licensed under the GNU General Public License v3.0
 * See the LICENSE file for details.
 */

package com.msg.end_less.features;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class NewEndPlatform {

    public static void createPlatform(ServerLevelAccessor level, BlockPos pos, boolean dropblock) {
        BlockPos.MutableBlockPos mutableBlockPos = pos.mutable();
        
        // A big air box with obby platform
        BlockPos blockPos;
        for(int i = 1; i < 10; ++i) {
            for(int j = -2; j < 3; ++j) {
                for(int k = -2; k < 3; ++k) {
                    blockPos = mutableBlockPos.set(pos).move(j, i, k);
                    placeBlock(level, blockPos, (i == 6 ? Blocks.OBSIDIAN : Blocks.AIR).defaultBlockState(), dropblock);
                }
            }
        }
        // End Portal
        placePortal((ServerLevel)level, pos, dropblock);
    }

    /**
     * Place the End Portal with an air box above it
     *
     * @param level        The current world/dimension.
     * @param pos          The block position.
     * @param dropblock    If true, drop block when break block.
     * @return The closest portal position, or null if none is close enough.
     */
    public static void placePortal(ServerLevel level, BlockPos pos, boolean dropblock) {
        BlockPos.MutableBlockPos mutableBlockPos = pos.mutable();
        BlockPos blockPos;
        for (int i = -2; i < 3; i++) {
            for (int j = -2; j < 3; j++) {
                blockPos = mutableBlockPos.set(pos).move(j, 0, i);
                BlockState state;

                if ((Math.abs(i) == 2 && Math.abs(j) == 2)) {
                    state = Blocks.AIR.defaultBlockState();
                }
                else {
                    state = Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.HAS_EYE, true);
                    if (i == 2) {
                        state = state.setValue(EndPortalFrameBlock.FACING, Direction.NORTH);
                    } else if (i == -2) {
                        state = state.setValue(EndPortalFrameBlock.FACING, Direction.SOUTH);
                    } else if (j == 2) {
                        state = state.setValue(EndPortalFrameBlock.FACING, Direction.WEST);
                    } else if (j == -2) {
                        state = state.setValue(EndPortalFrameBlock.FACING, Direction.EAST);
                    } else {
                        state = Blocks.END_PORTAL.defaultBlockState();
                    }
                }

                placeBlock(level, blockPos, state, dropblock);
            }
        }
    }

    /**
     * Destroy and replace the block at the position.
     *
     * @param level        The current world/dimension.
     * @param blockPos     The block position.
     * @param blockState   The blockState at the current position.
     * @param dropblock    If true, drop block when break block.
     */
    private static void placeBlock(ServerLevelAccessor level, BlockPos blockPos, BlockState blockState, boolean dropblock) {
        if (!level.getBlockState(blockPos).is(blockState.getBlock())) {
            level.destroyBlock(blockPos, dropblock);
            level.setBlock(blockPos, blockState, 3);
        }
    }

    /**
     * Finds the closest portal within a max distance.
     *
     * @param entity       The entity position to compare.
     * @param portals      A list of portal positions.
     * @param maxDistance  The max distance to consider.
     * @return The closest portal position, or null if none is close enough.
     */
    public static BlockPos findClosestPortalNear(Vec3 pos, List<BlockPos> portals, double maxDistance) {
        double maxSqr = maxDistance * maxDistance;

        for (BlockPos portalPos : portals) {
            double dx = portalPos.getX() - pos.x;
            double dz = portalPos.getZ() - pos.z;
            if ((dx * dx + dz * dz) < maxSqr) {
                return portalPos;
            }
        }

        return null;
    }
}
