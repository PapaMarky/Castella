package com.markyshouse.mc.Castella;

import com.markyshouse.mc.IBlockChooser;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

import java.util.HashMap;

/**
 * Created by mark on 9/20/2015.
 */
public interface ITemplateBlockChooser {
    public IBlockChooser chooseBlock(BlockPos pos, char template_value, World world, IChunkProvider chunkProvider);
}
