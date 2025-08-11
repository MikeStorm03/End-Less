/*
 * End-Less Mod
 * Copyright (c) 2025 MikeStorm03
 *
 * Licensed under the GNU General Public License v3.0
 * See the LICENSE file for details.
 */

package com.msg.dev_folder;

import java.util.Optional;

import com.msg.EndLessConstants;
import com.msg.features.NewEndPlatform;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ResourceLocationException;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class TestFunction {
    public static void place_portal(ServerLevel level, BlockPos blockPos) {
        StructureTemplateManager structureTemplateManager = level.getStructureManager();
        ResourceLocation portal = ResourceLocation.fromNamespaceAndPath(EndLessConstants.NAMESPACE, "island_portal");
        Optional optional = null;
        try {
            optional = structureTemplateManager.get(portal);
        } catch (ResourceLocationException var13) {
            EndLessConstants.LOG.info("Cannot spawn island portal.");
        }

        if (optional.isEmpty()) {
            EndLessConstants.LOG.info("Cannot find island portal to spawn.");
        } else {
            StructureTemplate structureTemplate = (StructureTemplate)optional.get();
            BlockPos portal_pos = new BlockPos(blockPos.getX() - structureTemplate.getSize().getX()/2, blockPos.getY(),blockPos.getZ() -  structureTemplate.getSize().getZ()/2);

            structureTemplate.placeInWorld(level, portal_pos, blockPos, new StructurePlaceSettings(), StructureBlockEntity.createRandom(0), 2);
        }
    }

    public static void init() {
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

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
                                                    dispatcher.register(Commands.literal("island_portal")
                                                            .then(Commands.argument("position", BlockPosArgument.blockPos())
                                                                    .executes(context -> {
                                                                        BlockPos blockPos = BlockPosArgument.getBlockPos(context, "position");
                                                                        place_portal(context.getSource().getLevel(), blockPos);;
                                                                        return 1;
                                                                    })));
            });
    }
}
