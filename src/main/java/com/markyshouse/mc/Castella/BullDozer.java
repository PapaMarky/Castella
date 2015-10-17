package com.markyshouse.mc.Castella;

/**
 * Created by mark on 9/18/2015.
 */
import com.markyshouse.mc.IBlockChooser;
import com.markyshouse.mc.TerrainMap;
import net.minecraft.block.*;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.IPlantable;

import java.util.Random;

public class BullDozer {
    enum ACTION {IGNORE, DESTROY, REPLACE}

    static public void setTerrainHeight(int x, int target_h, int z,
                                        Random random, World world, IChunkProvider chunkProvider,
                                        IBlockChooser blockChooser) {
        BlockPos pos = new BlockPos(x, target_h, z);
        Chunk chunk = chunkProvider.provideChunk(pos);
        int h = chunk.getHeight(pos);
        int ground = TerrainMap.getGroundLevel(new BlockPos(x, h, z), world, chunkProvider);
        int water = TerrainMap.getGroundOrWaterLevel(new BlockPos(x, h, z), world, chunkProvider);
        if (water > ground && blockChooser == null) {
            return;
        }
        BlockPos groundPos = new BlockPos(x, ground, z);
        // clear vegetation
        if (h > ground) {
            for(int y = h; y > ground; y--) {
                Block block = chunk.getBlock(new BlockPos(x, y, z));
                if (block instanceof IGrowable) {
                    world.destroyBlock(new BlockPos(x, y, z), false);
                }
            }
        }
        IBlockState groundTop = null;
        if (h > target_h) {
            //
            // *** need to dig ***
            //
            // find ground block
            for (int y = h; y >= ground; y--) {
                BlockPos bp = new BlockPos(x, y, z);
                Block block = chunk.getBlock(bp);
                if (TerrainMap.isGround(block)) {
                    groundTop = block.getActualState(block.getDefaultState(), world, bp);
                    break;
                }
            }
            for (int y = h; y > target_h; y--) {
                BlockPos bp = new BlockPos(x, y, z);
                Block block = chunk.getBlock(bp);
                boolean decayable = false;
                if (block instanceof  BlockLeaves) {
                    decayable = ((Boolean)block.getActualState(block.getDefaultState(), world, bp).getValue(BlockLeaves.DECAYABLE)).booleanValue();
                    if (decayable) {
                        world.destroyBlock(bp, false);
                    }
                } else if (!(block instanceof BlockLiquid)) {
                    world.destroyBlock(bp, false);
                }
                /**
                IBlockState glass = Blocks.glass.getDefaultState();
                if (!chunk.getBlock(new BlockPos(x, y, z)).isAir(null, null)) {
                    world.setBlockState(new BlockPos(x, y, z), glass);
                }
                **/
            }
            BlockPos target_pos = new BlockPos(x, target_h, z);

            IBlockState block = null;
            if (blockChooser != null) {
                block = blockChooser.chooseBlock(target_pos, world, chunkProvider);
                world.destroyBlock(target_pos, false);
                world.setBlockState(target_pos, block);
            } else {
                // replace with what was on top
                if (groundTop != null) {
                    world.destroyBlock(target_pos, false);
                    world.setBlockState(target_pos, groundTop);
                }
            }
            if (blockChooser == null) {
                block = world.getBlockState(target_pos);
            }

            // check for gaps under the tower and fill them in
            target_pos = target_pos.down();
            while(world.isAirBlock(target_pos)) {
                if (blockChooser != null) {
                    block = blockChooser.chooseBlock(target_pos, world, chunkProvider);
                }
                world.setBlockState(target_pos, block);
                target_pos = target_pos.down();
            }
        } else if (ground < target_h) {
            // *** need to fill ***
            Block b = chunk.getBlock(new BlockPos(x, ground, z));
            BlockPos bp = new BlockPos(x, ground, z);
            IBlockState block = b.getActualState(b.getDefaultState(), world, bp);
            if (world.isAirBlock(bp) || TerrainMap.isLiquid(block.getBlock())) {
                System.out.println("Filling With Water or Air");
            }

            for (int y = target_h; y > ground; y--) {
                BlockPos pos1 = new BlockPos(x, y, z);
                if (blockChooser != null) {
                    block = blockChooser.chooseBlock(pos1, world, chunkProvider);
                }
                world.destroyBlock(pos1, false);
                world.setBlockState(pos1, block);
                // if we are over water, don't fill
                if (water > ground) {
                    return;
                }
            }
        } else {
            // check for water
            if (blockChooser != null) {
                BlockPos blockPos = new BlockPos(x, target_h, z);
                world.destroyBlock(blockPos, false);
                world.setBlockState(blockPos, blockChooser.chooseBlock(blockPos, world, chunkProvider));
            }
        }
    }
    static public void bullDoze(BlockPos _pos, char[][] footprint,
                                IBlockChooser groundsBlockChooser,
                                IBlockChooser foundationBlockChooser,
                                Random random, World world, IChunkProvider chunkProvider) {
        int w = footprint[0].length;
        int h = footprint.length;
        for (int x = 0; x < w; x++) {
            int xx = x + _pos.getX();
            for (int z = 0; z < h; z++) {
                int zz = z + _pos.getZ();
                char fp = footprint[x][z];
                if (fp == ' ') {
                    continue;
                }
                int py = _pos.getY();

                if (fp ==  'g'){
                    IBlockState stone = Block.getStateById(5);
                    setTerrainHeight(xx, py, zz, random, world, chunkProvider, groundsBlockChooser);
                } else if (fp == 'W' || fp == 'F' || fp == 'D' || fp == 'c') {
                    setTerrainHeight(xx, py, zz, random, world, chunkProvider, foundationBlockChooser);
                } else if (fp >= '0' && fp <= '9'){
                    double pct = 1.0 - (double)(fp - '0')/10.0;
                    int ground = TerrainMap.getGroundLevel(new BlockPos(xx, py, zz), world, chunkProvider);
                    int slope_y = py + (int)Math.round((ground - py) * pct);
                    setTerrainHeight(xx, slope_y, zz, random, world, chunkProvider, null);
                }
            }
        }
    }
}
