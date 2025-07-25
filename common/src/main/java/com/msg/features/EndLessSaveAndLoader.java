/*
 * End-Less Mod
 * Copyright (c) 2025 MikeStorm03
 *
 * Licensed under the GNU General Public License v3.0
 * See the LICENSE file for details.
 */

package com.msg.features;

import java.util.ArrayList;
import java.util.List;

import com.msg.EndLessConstants;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

public class EndLessSaveAndLoader extends SavedData {

    public List<BlockPos> openEndPortal = new ArrayList<>();

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag listTag = new ListTag();
        for (BlockPos pos : openEndPortal) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("x", pos.getX());
            posTag.putInt("y", pos.getY());
            posTag.putInt("z", pos.getZ());
            listTag.add(posTag);
        }
        tag.put("openEndPortal", listTag);
        return tag;
    }

    public static EndLessSaveAndLoader createFromNbt(CompoundTag tag, HolderLookup.Provider registries) {
        EndLessSaveAndLoader state = new EndLessSaveAndLoader();

        ListTag listTag = tag.getList("openEndPortal", Tag.TAG_COMPOUND);
        for (Tag t : listTag) {
            CompoundTag posTag = (CompoundTag) t;
            int x = posTag.getInt("x");
            int y = posTag.getInt("y");
            int z = posTag.getInt("z");
            state.openEndPortal.add(new BlockPos(x, y, z));
        }

        return state;
    }

    public static EndLessSaveAndLoader createNew() {
        return new EndLessSaveAndLoader();
    }

    private static final SavedData.Factory<EndLessSaveAndLoader> type = new SavedData.Factory<>(
        EndLessSaveAndLoader::createNew,
        EndLessSaveAndLoader::createFromNbt,
        null
    );

    public static EndLessSaveAndLoader getServerState(MinecraftServer server) {
        ServerLevel level = server.getLevel(Level.OVERWORLD);
        assert level != null;
        EndLessSaveAndLoader state = level.getDataStorage().computeIfAbsent(type, EndLessConstants.ID);
        state.setDirty();
        return state;
    }
}
