package io.github.spiralhalo.plumbum.renderer.render;

import io.github.spiralhalo.plumbum.renderer.aocalc.AoCalculator;
import io.vram.frex.api.model.BlockModel;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.occlusion.BlockOcclusionCache;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockRenderView;

import java.util.function.Function;

public class TerrainRenderContext {
	private final BlockRenderInfo blockInfo = new BlockRenderInfo();
	private final ChunkRenderInfo chunkInfo = new ChunkRenderInfo();
	private final AoCalculator aoCalc = new AoCalculator(blockInfo, chunkInfo::cachedBrightness, chunkInfo::cachedAoLevel);

	private Vec3i origin;
	private Vec3d modelOffset;

	private final BaseMeshConsumer meshConsumer = new BaseMeshConsumer(new QuadBufferer(chunkInfo::getChunkModelBuilder), blockInfo, aoCalc);

	public void prepare(BlockRenderView blockView, ChunkBuildBuffers buffers, BlockOcclusionCache cache) {
		blockInfo.setBlockOcclusionCache(cache);
		blockInfo.setBlockView(blockView);
		chunkInfo.prepare(blockView, buffers);
	}

	public void release() {
		blockInfo.release();
		chunkInfo.release();
	}

	/** Called from chunk renderer hook. */
	public boolean tessellateBlock(BlockState blockState, BlockPos blockPos, BlockPos origin, final BakedModel model, Vec3d modelOffset) {
		this.origin = origin;
		this.modelOffset = modelOffset;

		try {
			chunkInfo.didOutput = false;
			aoCalc.clear();
			blockInfo.prepareForBlock(model, blockState, blockPos);
			((BlockModel) model).renderDynamic(blockInfo, meshConsumer.getEmitter());
		} catch (Throwable throwable) {
			CrashReport crashReport = CrashReport.create(throwable, "Tessellating block in world - Plumbum Renderer");
			CrashReportSection crashReportSection = crashReport.addElement("Block being tessellated");
			CrashReportSection.addBlockInfo(crashReportSection, chunkInfo.blockView, blockPos, blockState);
			throw new CrashException(crashReport);
		}

		return chunkInfo.didOutput;
	}

	private class QuadBufferer extends ChunkQuadBufferer {
		QuadBufferer(Function<RenderLayer, ChunkModelBuilder> builderFunc) {
			super(builderFunc);
		}

		@Override
		protected Vec3i origin() {
			return origin;
		}

		@Override
		protected Vec3d blockOffset() {
			return modelOffset;
		}
	}
}
