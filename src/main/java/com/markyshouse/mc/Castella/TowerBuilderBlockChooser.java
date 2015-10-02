package com.markyshouse.mc.Castella;

import com.markyshouse.mc.IBlockChooser;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

/**
 * Created by mark on 9/20/2015.
 */
public class TowerBuilderBlockChooser {
    protected IBlockChooser wallChooser;
    protected IBlockChooser floorChooser;
    protected IBlockChooser windowChooser;
    protected IBlockChooser doorChooser;
    protected IBlockChooser slopeChooser;
    protected IBlockChooser groundsChooser;

    public IBlockState chooseBlock(BlockPos pos, char template_value, World world, IChunkProvider chunkProvider) {
        return null;
    }
}
