/*
 * End-Less Mod
 * Copyright (c) 2025 MikeStorm03
 *
 * Licensed under the GNU General Public License v3.0
 * See the LICENSE file for details.
 */

package com.msg;

import com.msg.platform.Services;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class EndLessCommon {

	// Custom dimension
    public static final ResourceKey<Level> NEW_END = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(EndLessConstants.NAMESPACE, "new_end"));

	// A data saver for all of open portal in the world
    public record EndPortalsPos(BlockPos blockPos) implements CustomPacketPayload {
		public static final ResourceLocation OPENED_END_PORTAL = ResourceLocation.fromNamespaceAndPath(EndLessConstants.NAMESPACE, "openEndPortal");
		public static final CustomPacketPayload.Type<EndPortalsPos> ID = new CustomPacketPayload.Type<>(OPENED_END_PORTAL);
		public static final StreamCodec<FriendlyByteBuf, EndPortalsPos> CODEC = StreamCodec.composite(
				StreamCodec.of(
					(buf, pos) -> buf.writeLong(pos.asLong()),
					buf -> BlockPos.of(buf.readLong())),
				EndPortalsPos::blockPos,
				EndPortalsPos::new);
	
		@Override
		public Type<? extends CustomPacketPayload> type() {
			return ID;
		}
	}

    public static void init() {
        EndLessConstants.LOG.info("Mod {} is running on {} in {} environment!", EndLessConstants.NAME, Services.PLATFORM.getPlatformName(), Services.PLATFORM.getEnvironmentName());
    
	}
}