package com.markyshouse.mc;

import net.minecraft.block.Block;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mark on 9/29/2015.
 */
public class TerrainMap {
    public class Square {
        BiomeGenBase biome;
        int topLevel; // top of trees, grass, etc
        Block topBlock;
        int groundLevel; // topmost solid block (dirt, stone, etc)
        Block groundBlock;
        boolean isUnderWater; // is ground level below water?
        int waterLevel;
        boolean isUnderLava;
        int lavalLevel;
    }

    private Square[][] _map = null;
    private ArrayList<BiomeGenBase> _biomeList = null;
    private int max_height = 0;
    private int min_height = 0;
    private int water_level = 0;
    private double build_height = 0;

    private int _width = 0;
    private int _height = 0;

    public int minHeight() { return min_height; }
    public int maxHeight() { return max_height; }
    public int averageHeight() { return min_height + (max_height - min_height) / 2; }

    public List<BiomeGenBase> getBiomeList() { return _biomeList; }

    public boolean isLevelEnough() {
        boolean isLevel = max_height - Math.max(water_level, min_height) < Math.min(_width, _height);
        //if (!isLevel)
        //    System.out.println("-- NotLevel");
        return isLevel;
    }

    public double getBuildHeight() { return build_height; }
    public TerrainMap(BlockPos origin, int w, int h, World world, IChunkProvider chunkProvider) {
        _width = w;
        _height = h;
        _map = new Square[_width][_height];
        _biomeList = new ArrayList<BiomeGenBase>();
        max_height = 0;
        min_height = 4096; // arbitrarily large number
        for (int x = 0; x < w; x++) {
            for (int z = 0; z < h; z++) {
                BlockPos pos = new BlockPos(origin.getX() + x, origin.getY(), origin.getZ() + z);
                Chunk chunk = chunkProvider.provideChunk(pos);
                BlockPos groundPos = world.getTopSolidOrLiquidBlock(pos);
                Block block = chunk.getBlock(groundPos);
                while (!isGround(block) && !isLiquid(block)) {
                    groundPos = groundPos.down();
                    block = chunk.getBlock(groundPos);
                }
                _map[x][z] = new Square();
                _map[x][z].isUnderWater = false;
                _map[x][z].waterLevel = 0;
                Material material = block.getMaterial();
                if (material == Material.water) {
                    _map[x][z].isUnderWater = true;
                    _map[x][z].waterLevel = groundPos.getY();
                    if (groundPos.getY() > water_level) water_level = groundPos.getY();
                    while (material == Material.water) {
                        groundPos = groundPos.down();
                        material = chunk.getBlock(groundPos).getMaterial();
                    }
                }
                _map[x][z].isUnderLava = false;
                _map[x][z].lavalLevel = 0;
                if (material == Material.lava) {
                    if (groundPos.getY() > water_level) water_level = groundPos.getY();
                    _map[x][z].isUnderLava = true;
                    _map[x][z].lavalLevel = groundPos.getY();
                    while (material == Material.lava) {
                        groundPos = groundPos.down();
                        material = chunk.getBlock(groundPos).getMaterial();
                    }
                }
                int ground = groundPos.getY();
                _map[x][z].groundLevel = ground;
                if (ground > max_height) {
                    max_height = ground;
                }
                if (ground < min_height) {
                    min_height = ground;
                }
                _map[x][z].groundBlock = chunk.getBlock(groundPos);
                BiomeGenBase biome = chunk.getBiome(pos, world.getWorldChunkManager());
                if (!_biomeList.contains(biome)) _biomeList.add(biome);
                _map[x][z].biome = biome;
            }
        }
        build_height = Math.max(water_level, min_height + (int)Math.round((double)(max_height - min_height)/2.0));
    }

    public int heightAt(int x, int z) {
        if (_map == null) return -1;
        return _map[x][z].groundLevel;
    }
    static public boolean isLiquid(Block block) {
        Material m = block.getMaterial();
        return (m == Material.water || m == Material.lava);
    }
    static public boolean isGround(Block block) {
        Material m = block.getMaterial();
        return (m == Material.ground || m == Material.clay ||
                m == Material.rock || m == Material.sand || m == Material.grass) ;
    }
    static public int getTreeType(BlockPos blockPos, World world, IChunkProvider chunkProvider) {
        Chunk chunk = chunkProvider.provideChunk(blockPos);
        Block block = chunk.getBlock(blockPos);
        if (! block.isWood(world, blockPos)) {
            return -1;
        }
        IBlockState bs = block.getActualState(block.getDefaultState(), world, blockPos);
        int meta = -1;

        Object obj = bs.getValue(BlockNewLog.VARIANT);
        if (obj instanceof BlockPlanks.EnumType) {
            meta = ((BlockPlanks.EnumType)obj).getMetadata();
        }
        return meta;
    }

    static public int getGroundLevel(BlockPos pos, World world, IChunkProvider chunkProvider) {
        Chunk chunk = chunkProvider.provideChunk(pos);
        BlockPos groundPos = world.getTopSolidOrLiquidBlock(pos);
        Material material = chunk.getBlock(groundPos).getMaterial();
        while(!isGround(chunk.getBlock(groundPos))) {
            groundPos = groundPos.down();
        }
        return groundPos.getY();
    }
}
