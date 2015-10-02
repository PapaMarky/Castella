package com.markyshouse.mc.Castella;

/**
 * Created by mark on 9/19/2015.
 */
import com.markyshouse.mc.IBlockChooser;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

public class SimpleBlockChooser implements IBlockChooser {
    public SimpleBlockChooser(IBlockState state) {
        blockState = state;
    }
    public IBlockState chooseBlock(BlockPos pos, World world, IChunkProvider chunkProvider) {
        return blockState;
    }
    IBlockState blockState = null;
}
