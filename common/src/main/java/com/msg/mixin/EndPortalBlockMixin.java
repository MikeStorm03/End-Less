/*
 * End-Less Mod
 * Copyright (c) 2025 MikeStorm03
 *
 * Licensed under the GNU General Public License v3.0
 * See the LICENSE file for details.
 */

package com.msg.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.levelgen.feature.EndPlatformFeature;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.msg.EndLessCommon;
import com.msg.features.EndLessSaveAndLoader;
import com.msg.features.NewEndPlatform;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {
    
    @Overwrite
    public DimensionTransition getPortalDestination(ServerLevel level, Entity entity, BlockPos pos) {

        double[] entityPos = {entity.getX(), entity.getZ()};
        ResourceKey<Level> dimension = level.dimension();
        BlockPos blockPos;
        EndDragonFight dragonFight = level.getDragonFight();
        // set the destination base on the current location
        if ((dragonFight == null ? true : dragonFight.hasPreviouslyKilledDragon() ) || // none of the dragon have been killed
            (entityPos[0] > -20 && entityPos[0] < 20 && entityPos[1] > -20 && entityPos[1] < 20 && dimension == Level.OVERWORLD))
            // there are dead dragon already and entity location is [ -20 < x < 20 ; -20 < z < 20] in the overworld
        { 
                dimension = Level.END;
                blockPos = ServerLevel.END_SPAWN_POINT;
                
                ServerLevel serverLevel = level.getServer().getLevel(dimension);
                if (serverLevel == null) {
                    return null;
                } else {
                    Vec3 vec3 = blockPos.getBottomCenter();
                    EndPlatformFeature.createEndPlatform(serverLevel, BlockPos.containing(vec3).below(), true);
                    if (entity instanceof ServerPlayer) {
                        vec3 = vec3.subtract(0.0, 1.0, 0.0);
                    }

                    return new DimensionTransition(serverLevel,
                                                    vec3,
                                                    entity.getDeltaMovement(),
                                                    Direction.WEST.toYRot(),
                                                    entity.getXRot(),
                                                    DimensionTransition.PLAY_PORTAL_SOUND.then(DimensionTransition.PLACE_PORTAL_TICKET));
                }
        } else if (dimension == EndLessCommon.NEW_END) { // <- new dimension
                dimension = Level.OVERWORLD;
                ServerLevel serverLevel = level.getServer().getLevel(dimension);
                if (serverLevel == null) {
                    return null;
                } else {
                    // to Overworld
                    Vec3 vec3 = null;
                    EndLessSaveAndLoader openPortals = EndLessSaveAndLoader.getServerState(level.getServer()); // get all of the opened portal in the overworld
                    for (BlockPos i : openPortals.openEndPortal) { // check each of the opened portal
                        if (Math.sqrt(Math.pow(i.getX()-entityPos[0], 2) + Math.pow(i.getZ()-entityPos[1], 2)) < 5) { // find the closest one | SQRT( (Xi-Xp)^2 + (Zi-Zp)^2 ) = SQRT(X^2+Z^2) = distance

                            // Create a list of posible safe spot that player can stand on
                            BlockPos.MutableBlockPos mutableBlockPos = i.mutable();
                            List<BlockPos> blockPosList = new ArrayList<BlockPos>();
                            for (int m = -7; m < 8; m++) {
                                for (int n = -7; n < 8; n++) {
                                    for (int o = -5; o < 5; o++) {
                                        if (o == 0 && -2 < m && m < 2 && -2 < n && n < 2) {
                                            continue;
                                        }
                                        blockPosList.add(mutableBlockPos.set(i).move(m, 0, n));
                                    }
                                }
                            }
                            // Shuffle it to create some randomness
                            Collections.shuffle(blockPosList);
                            for (BlockPos j : blockPosList) {
                                if (isSafe(serverLevel, j)) {
                                    vec3 = j.getBottomCenter();
                                    return new DimensionTransition(serverLevel, vec3, entity.getDeltaMovement(), entity.getYRot(), entity.getXRot(), DimensionTransition.PLAY_PORTAL_SOUND.then(DimensionTransition.PLACE_PORTAL_TICKET));
                                }
                            }
                        }
                    }

                    return toSpawnPoint(serverLevel, entity);
                }
        } else if (dimension == Level.OVERWORLD) { // <- from OverWorld to new dimension
                dimension = EndLessCommon.NEW_END;
                
                ServerLevel serverLevel = level.getServer().getLevel(dimension);
                if (serverLevel == null) {
                    return null;
                } else {
                    Vec3 vec3 = null;
                    EndLessSaveAndLoader openPortals = EndLessSaveAndLoader.getServerState(level.getServer()); // get all of the opened portal in the overworld
                    for (BlockPos i : openPortals.openEndPortal) { // check each of the opened portal
                        if (Math.sqrt(Math.pow(i.getX()-entityPos[0], 2) + Math.pow(i.getZ()-entityPos[1], 2)) < 5) { // find the closest one | SQRT( (Xi-Xp)^2 + (Zi-Zp)^2 ) = SQRT(X^2+Z^2) = distance
                            vec3 = new Vec3(entityPos[0], 55, entityPos[1]);
                            NewEndPlatform.createPlatform(serverLevel, new BlockPos(i.getX(), 48, i.getZ()));

                            return new DimensionTransition(serverLevel, vec3, entity.getDeltaMovement(), entity.getYRot(), entity.getXRot(), DimensionTransition.PLAY_PORTAL_SOUND.then(DimensionTransition.PLACE_PORTAL_TICKET));
                        }
                    }
                    NewEndPlatform.createPlatform(serverLevel, new BlockPos(0, 48, 0));
                    return new DimensionTransition(serverLevel,
                                                    entity.adjustSpawnLocation(serverLevel, serverLevel.getSharedSpawnPos()).getBottomCenter(),
                                                    entity.getDeltaMovement(),
                                                    entity.getYRot(),
                                                    entity.getXRot(),
                                                    DimensionTransition.PLAY_PORTAL_SOUND.then(DimensionTransition.PLACE_PORTAL_TICKET));
                }
        } else { // <- from The End -> spawn point
                return toSpawnPoint(level, entity);
        }
    }

    private static Boolean isSafe(ServerLevel level, BlockPos blockPos) {
        BlockState aboveBlock = level.getBlockState(blockPos.above());
        FluidState checkFluid = aboveBlock.getFluidState();
        return (level.getBlockState(blockPos.below()).isFaceSturdy(level, blockPos, Direction.UP) &&
                    (aboveBlock.isAir() ||
                    !checkFluid.isEmpty()) ||
                    aboveBlock.getBlock() == checkFluid.getType().defaultFluidState().createLegacyBlock().getBlock());
    }

    private static DimensionTransition toSpawnPoint(ServerLevel level, Entity entity){
        if (entity instanceof ServerPlayer) {
                    ServerPlayer serverPlayer = (ServerPlayer)entity;
                    return serverPlayer.findRespawnPositionAndUseSpawnBlock(false, DimensionTransition.DO_NOTHING);
                }
                ServerLevel serverLevel = level.getServer().getLevel(Level.OVERWORLD);
                return serverLevel == null ? null : new DimensionTransition(serverLevel,
                                                                            entity.adjustSpawnLocation(serverLevel, serverLevel.getSharedSpawnPos()).getBottomCenter(),
                                                                            entity.getDeltaMovement(),
                                                                            entity.getYRot(),
                                                                            entity.getXRot(),
                                                                            DimensionTransition.PLAY_PORTAL_SOUND.then(DimensionTransition.PLACE_PORTAL_TICKET));
    }
}