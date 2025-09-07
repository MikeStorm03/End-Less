/*
 * End-Less Mod
 * Copyright (c) 2025 MikeStorm03
 *
 * Licensed under the GNU General Public License v3.0
 * See the LICENSE file for details.
 */

package com.msg.end_less.features;

import java.util.ArrayList;
import java.util.List;

import com.msg.end_less.EndLessConstants;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
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
            listTag.add(new IntArrayTag(new int[]{pos.getX(), pos.getY(), pos.getZ()}));
        }
        tag.put("openEndPortal", listTag);
        return tag;
    }

    public static EndLessSaveAndLoader createFromNbt(CompoundTag tag, HolderLookup.Provider registries) {
        EndLessSaveAndLoader state = new EndLessSaveAndLoader();

        ListTag listTag = tag.getList("openEndPortal", Tag.TAG_INT_ARRAY);

        if (listTag.isEmpty()) { // to the dected the old saving structure and update it to new structure.
            EndLessConstants.LOG.info("Detected old opened portals save file, updating!");

            listTag = tag.getList("openEndPortal", Tag.TAG_COMPOUND);
            for (Tag t : listTag) {
                CompoundTag posTag = (CompoundTag) t;
                state.openEndPortal.add(new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z")));
            }
        }
        else {
            for (Tag t : listTag) {
                int[] pos = (((IntArrayTag) t).getAsIntArray());
                state.openEndPortal.add(new BlockPos(pos[0], pos[1], pos[2]));
            }
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
        EndLessSaveAndLoader state = level.getDataStorage().computeIfAbsent(type, EndLessConstants.ID);
        state.setDirty();
        return state;
    }
}
