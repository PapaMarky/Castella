package com.markyshouse.mc;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

/**
 * Created by mark on 9/15/2015.
 */
class HeightAnalyser {
    public int max_height;
    public int min_height;
    public float average_height;
    public float mean_height;
    World _world;
    IChunkProvider _chunkProvider;

    public HeightAnalyser(World world, IChunkProvider chunkProvider) {
        _world = world;
        _chunkProvider = chunkProvider;
    }

    static public int get_ground_level(BlockPos pos, IChunkProvider chunkProvider) {
        Chunk chunk = chunkProvider.provideChunk(pos);
        int h = chunk.getHeight(pos);
        pos = new BlockPos(pos.getX(), h, pos.getZ());
        Block b1 = chunk.getBlock(pos);
        while(isGround(b1)) {
            pos = pos.up();
            b1 = chunk.getBlock(pos);
        }
        while(!isGround(b1)) {
            pos = pos.down();
            b1 = chunk.getBlock(pos);
        }
        return pos.getY();
    }
    static public boolean isGround(Block block) {
        Material m = block.getMaterial();
        return (m == Material.ground || m == Material.clay || m == Material.glass ||
                m == Material.rock || m == Material.sand || m == Material.grass) ;
    }
    public void analyze(BlockPos center, int width_x, int width_z) {
        int radius = width_x/2;
        int start_x = center.getX() - (width_x / 2);
        int start_z = center.getZ() - (width_z / 2);
        max_height = min_height = -1;
        long total = 0;
        long count = 0;
        for (int x = start_x; x < start_x + width_x; x++) {
            for (int z = start_z; z < start_z + width_z; z++) {
                int dx = x - center.getX();
                int dz = z - center.getZ();
                int dist = (int)Math.floor(Math.sqrt(dx*dx + dz*dz));
                if (dist >= width_x) {
                    continue;
                }

                BlockPos pos = new BlockPos(x, center.getY(), z);
                Chunk chunk = _chunkProvider.provideChunk(pos);
                int h = get_ground_level(pos, _chunkProvider);
                if (max_height == -1 || max_height < h) {
                    max_height = h;
                }
                if (min_height == -1 || h < min_height) {
                    min_height = h;
                }
                total += h;
                count++;
            }
        }
        average_height = min_height + (int)Math.round((double)(max_height - min_height)/2.0);
    }
}
