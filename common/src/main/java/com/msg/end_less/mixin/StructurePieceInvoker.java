package com.msg.end_less.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.levelgen.structure.StructurePiece;

@Mixin(StructurePiece.class)
public interface StructurePieceInvoker {
    @Invoker("getWorldPos")
    public MutableBlockPos getWorPos(int x, int y, int z);
}
