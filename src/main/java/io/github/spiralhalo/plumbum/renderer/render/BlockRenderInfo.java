/*
 * Copyright (c) 2016-2022 Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.spiralhalo.plumbum.renderer.render;

import io.vram.frex.api.material.MaterialConstants;
import io.vram.frex.base.renderer.context.input.BaseBlockInputContext;
import me.jellysquid.mods.sodium.client.render.occlusion.BlockOcclusionCache;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

import java.util.Random;
import java.util.function.Supplier;

/**
 * Holds, manages and provides access to the block/world related state
 * needed by fallback and mesh consumers.
 *
 * <p>Exception: per-block position offsets are tracked in {@link ChunkRenderInfo}
 * so they can be applied together with chunk offsets.
 */
public class BlockRenderInfo extends BaseBlockInputContext<BlockRenderView> {
	private final BlockColors blockColorMap = MinecraftClient.getInstance().getBlockColors();
	protected BlockOcclusionCache blockOcclusionCache;
	public final Random random = new Random();
	public long seed;
	boolean defaultAo;
	RenderLayer defaultLayer;

	public final Supplier<Random> randomSupplier = () -> {
		final Random result = random;
		long seed = this.seed;

		if (seed == -1L) {
			seed = blockState.getRenderingSeed(blockPos);
			this.seed = seed;
		}

		result.setSeed(seed);
		return result;
	};

	public void setBlockView(BlockRenderView blockView) {
		this.blockView = blockView;
	}

	public void setBlockOcclusionCache(BlockOcclusionCache blockOcclusionCache) {
		this.blockOcclusionCache = blockOcclusionCache;
	}

	public void prepareForBlock(BakedModel bakedModel, BlockState blockState, BlockPos blockPos) {
		super.prepareForBlock(bakedModel, blockState, blockPos);
		this.blockPos = blockPos;
		this.blockState = blockState;
		// in the unlikely case seed actually matches this, we'll simply retrieve it more than one
		seed = -1L;
		defaultAo = bakedModel.useAmbientOcclusion() && MinecraftClient.isAmbientOcclusionEnabled() && blockState.getLuminance() == 0;

		defaultLayer = RenderLayers.getBlockLayer(blockState);
	}

	public void release() {
		super.release();
		blockPos = null;
		blockState = null;
		blockOcclusionCache = null;
	}

	int blockColor(int colorIndex) {
		return 0xFF000000 | blockColorMap.getColor(blockState, blockView, blockPos, colorIndex);
	}

	boolean shouldDrawFace(Direction face) {
		return true;
	}

	RenderLayer effectiveRenderLayer(int preset) {
		return switch (preset) {
			case MaterialConstants.PRESET_SOLID -> RenderLayer.getSolid();
			case MaterialConstants.PRESET_CUTOUT_MIPPED -> RenderLayer.getCutoutMipped();
			case MaterialConstants.PRESET_CUTOUT -> RenderLayer.getCutout();
			case MaterialConstants.PRESET_TRANSLUCENT -> RenderLayer.getTranslucent();
			default -> defaultLayer;
		};
	}
}
