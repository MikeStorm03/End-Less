/*
 * End-Less Mod
 * Copyright (c) 2025 MikeStorm03
 *
 * Licensed under the GNU General Public License v3.0
 * See the LICENSE file for details.
 */

package com.msg;

import com.msg.features.NewEndPlatform;
import com.msg.platform.Services;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;


public class EndlessFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        
        EndLessCommon.init();
        if (Services.PLATFORM.isDevelopmentEnvironment()) {
            // Command that spawn new End Platform at <x> <y> <z>
            CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
                                                        dispatcher.register(Commands.literal("newEndPlatform")
                                                                .then(Commands.argument("position", BlockPosArgument.blockPos())
                                                                        .executes(context -> {
                                                                            BlockPos blockPos = BlockPosArgument.getBlockPos(context, "position");
                                                                            NewEndPlatform.createPlatform(context.getSource().getLevel(), blockPos);
                                                                            return 1;
                                                                        })));
            });
		}
    }
}
