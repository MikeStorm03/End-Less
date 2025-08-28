/*
 * End-Less Mod
 * Copyright (c) 2025 MikeStorm03
 *
 * Licensed under the GNU General Public License v3.0
 * See the LICENSE file for details.
 */

package com.msg.end_less.features;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class NewEndPlatform extends Feature<NoneFeatureConfiguration> {
    public NewEndPlatform(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        createPlatform(context.level(), context.origin());
        return true;
    }

    public static void createPlatform(ServerLevelAccessor level, BlockPos pos) {
        BlockPos.MutableBlockPos mutableBlockPos = pos.mutable();
        // A big air box with obby platform
        BlockPos blockPos;
        for(int i = 1; i < 10; ++i) {
            for(int j = -2; j < 3; ++j) {
                for(int k = -2; k < 3; ++k) {
                blockPos = mutableBlockPos.set(pos).move(j, i, k);
                placeBlock(level, blockPos, (i == 6 ? Blocks.OBSIDIAN : Blocks.AIR).defaultBlockState());
                }
            }
        }
        // End Portal
        placePortal((ServerLevel)level, pos);
    }

    public static void placePortal(ServerLevel level, BlockPos pos) {
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

                placeBlock(level, blockPos, state);
            }
        }
    }

    private static void placeBlock(ServerLevelAccessor level, BlockPos blockPos, BlockState blockState) {
        if (!level.getBlockState(blockPos).is(blockState.getBlock())) {
            level.setBlock(blockPos, blockState, 3);
        }
    }
}
