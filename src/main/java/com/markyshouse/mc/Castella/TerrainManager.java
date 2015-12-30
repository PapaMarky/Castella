package com.markyshouse.mc.Castella;

import com.google.common.collect.ImmutableCollection;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.block.properties.IProperty;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mark on 9/15/2015.
 */
class TerrainManager {
    public int max_height;
    public int min_height;
    public float build_height;
    public int water_level;
    //public float mean_height;
    World _world;
    IChunkProvider _chunkProvider;

    static public boolean isTree(Block block) {
        return (block instanceof BlockLog || block instanceof BlockLeaves || block instanceof BlockCocoa || block instanceof BlockSapling);
    }

    static public int getTreeType(BlockPos blockPos, World world, IChunkProvider chunkProvider) {
        Chunk chunk = chunkProvider.provideChunk(blockPos);
        Block block = chunk.getBlock(blockPos);
        if (! isTree(block)) {
            return -1;
        }
        IBlockState bs = world.getBlockState(blockPos);
        int meta = -1;

        Object obj = bs.getValue(BlockNewLog.VARIANT);
        if (obj instanceof BlockPlanks.EnumType) {
            meta = ((BlockPlanks.EnumType)obj).getMetadata();
        }
        return meta;
    }
    public TerrainManager(World world, IChunkProvider chunkProvider) {
        _world = world;
        _chunkProvider = chunkProvider;
    }

    List<BiomeGenBase> biomeList = null;
    int[][] topMap = null;
    public void analyze(BlockPos origin, char[][] footprint) {
        int width_x = footprint[0].length;
        int width_z = footprint.length;
        int start_x = origin.getX();
        int start_z = origin.getZ();
        max_height = min_height = -1;
        water_level = 0;
        long total = 0;
        long count = 0;
        biomeList = new ArrayList<BiomeGenBase>();
        topMap = new int[width_x][width_z];
        for (int x = 0; x < width_x; x++) {
            for (int z = 0; z < width_z; z++) {
                char f = footprint[x][z];

                if (f == ' ') {
                    continue;
                }

                BlockPos pos = new BlockPos(x + start_x, origin.getY(), z + start_z);
                Chunk chunk = _chunkProvider.provideChunk(pos);
                topMap[x][z] = chunk.getHeight(pos);
                BiomeGenBase biome = chunk.getBiome(pos, _world.getWorldChunkManager());
                if (!biomeList.contains(biome)) {
                    biomeList.add(biome);
                }
                /*
                int h = get_ground_level(pos, _chunkProvider);
                get_water_level(pos, _chunkProvider);
                if (max_height == -1 || max_height < h) {
                    max_height = h;
                }
                if (min_height == -1 || h < min_height) {
                    min_height = h;
                }
                total += h;
                count++;
                */
            }
        }
        build_height = Math.max(water_level, min_height + (int)Math.round((double)(max_height - min_height)/2.0));
    }
}
