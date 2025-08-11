package com.msg.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.msg.EndLessConstants;

import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
@Mixin(EndDragonFight.class)
public class DragonFightMixin {
    
    @Shadow public boolean previouslyKilled;
    @Shadow private ServerLevel level;

    @Inject(method = "Lnet/minecraft/world/level/dimension/end/EndDragonFight;setDragonKilled(Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;)V",
            at = @At("HEAD"))
    private void firstDragonKilled(EnderDragon enderDragon, CallbackInfo ci){
        if (!this.previouslyKilled){
            EndLessConstants.LOG.info("Trying to spawn the portal");
            place_portal(level.getServer().getLevel(level.OVERWORLD));
        }
    }

    private static void place_portal(ServerLevel level) {
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

            structureTemplate.placeInWorld(level,
                                            new BlockPos(- structureTemplate.getSize().getX()/2 + 1, 300, - structureTemplate.getSize().getZ()/2),
                                            new BlockPos(0, 300, 0),
                                            new StructurePlaceSettings(),
                                            StructureBlockEntity.createRandom(0), 2);
        }
    }
}
