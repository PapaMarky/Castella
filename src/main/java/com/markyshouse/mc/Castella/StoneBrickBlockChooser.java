package com.markyshouse.mc.Castella;

import com.markyshouse.mc.IBlockChooser;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

/**
 * Created by mark on 9/16/2015.
 */
public class StoneBrickBlockChooser implements IBlockChooser {
    public IBlockState chooseBlock(BlockPos pos, World world, IChunkProvider chunkProvider) {
        return Blocks.stonebrick.getDefaultState();
    }
}
