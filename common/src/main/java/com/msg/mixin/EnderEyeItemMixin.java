/*
 * End-Less Mod
 * Copyright (c) 2025 MikeStorm03
 *
 * Licensed under the GNU General Public License v3.0
 * See the LICENSE file for details.
 */

package com.msg.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.msg.features.EndLessSaveAndLoader;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.EnderEyeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

@Mixin(EnderEyeItem.class)
public class EnderEyeItemMixin {
    @Inject(
        method = "useOn",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;globalLevelEvent(ILnet/minecraft/core/BlockPos;I)V"
        )
    )
    private void onEndPortalBlockPlaced(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        Level world = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        EndLessSaveAndLoader openPortals = EndLessSaveAndLoader.getServerState(world.getServer());
        for (int i = -2; i < 3; i++) {
            for (int j = -2; j < 3; j++) {
                if (i > -2  && i < 2 && j > -2 && j < 2){
                    continue;
                } else {
                    BlockPos cornerPos = blockPos.offset(i, 0, j);

                    if (checkSquare(world, cornerPos)) {
                        BlockPos centerPos = cornerPos.offset(1, 0, 1);
                        openPortals.openEndPortal.add(centerPos);
                        return;
                    }
                }
            }
        }
    }

    private static boolean checkSquare(Level world, BlockPos blockPos){
        for (int i = -1; i < 1; i++){
            for (int j = -1; j < 1; j++){
                if (!world.getBlockState(blockPos).is(Blocks.END_PORTAL)){
                    return false;
                }
            }
        }
        return true;
    }
}
