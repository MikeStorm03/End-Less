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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.msg.end_less.EndLessConstants;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class EndLessSaveAndLoader extends SavedData {

    public List<BlockPos> openEndPortal = new ArrayList<>();

    public EndLessSaveAndLoader(List<BlockPos> openEndPortal) {
        this.openEndPortal = openEndPortal;
    }

    public static final Codec<EndLessSaveAndLoader> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            BlockPos.CODEC.listOf().fieldOf("openEndPortal").forGetter(state -> state.openEndPortal)
        ).apply(instance, EndLessSaveAndLoader::new)
    );


    public static EndLessSaveAndLoader createNew() {
        EndLessSaveAndLoader state = new EndLessSaveAndLoader(new ArrayList<>());
        return state;
    }

    private static final SavedDataType<EndLessSaveAndLoader> type = new SavedDataType<>(
        EndLessConstants.ID,
        EndLessSaveAndLoader::createNew,
        CODEC,
        null
    );

    public static EndLessSaveAndLoader getServerState(MinecraftServer server) {
        EndLessSaveAndLoader state = server.getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(type);
        state.openEndPortal = new ArrayList<>(state.openEndPortal);
        state.setDirty();
        return state;
    }
}
