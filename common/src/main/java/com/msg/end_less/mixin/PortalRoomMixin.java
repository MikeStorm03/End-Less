package com.msg.end_less.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.msg.end_less.features.EndLessSaveAndLoader;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.structures.StrongholdPieces.PortalRoom;

@Mixin(PortalRoom.class)
public class PortalRoomMixin {
    @Inject(method = "postProcess",
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/level/levelgen/structure/structures/StrongholdPieces$PortalRoom;placeBlock(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/block/state/BlockState;IIILnet/minecraft/world/level/levelgen/structure/BoundingBox;)V",
                ordinal = 20
            ))
    private void inject(WorldGenLevel level,
                        StructureManager structureManager,
                        ChunkGenerator generator,
                        RandomSource random,
                        BoundingBox box,
                        ChunkPos chunkPos,
                        BlockPos pos,
                        CallbackInfo cir) {
        BlockPos blockPos = ((StructurePieceInvoker)this).getWorPos(5, 3, 10);
        EndLessSaveAndLoader openPortals = EndLessSaveAndLoader.getServerState(level.getServer());
        openPortals.openEndPortal.add(blockPos);
    }
}
