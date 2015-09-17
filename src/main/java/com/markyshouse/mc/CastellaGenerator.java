package com.markyshouse.mc;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.HashMap;
import java.util.Random;

/**
 * Created by mark on 9/13/2015.
 */
public class CastellaGenerator  implements IWorldGenerator {
    static private boolean done = false;
    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
        switch (world.provider.getDimensionId()) {
            case 0: //Overworld
                if (done) {
                    return;
                }
                WorldInfo world_info = world.getWorldInfo();
                NBTBase CastellaInfo = this.getAdditionalInfo(world_info);
                System.out.println(" ** OVERWORLD chunk " + chunkX + ", " + chunkZ + " **");
                int spawn_x = world_info.getSpawnX();
                int spawn_z = world_info.getSpawnZ();
                int spawn_y = world_info.getSpawnY();
                BlockPos spawn_pos = new BlockPos(spawn_x, spawn_y, spawn_z);
                Chunk spawn_chunk = chunkProvider.provideChunk(spawn_pos);
                int height = spawn_chunk.getHeight(spawn_pos);
                IBlockState bs = Blocks.sandstone.getDefaultState();
                Block b = spawn_chunk.getBlock(spawn_pos);
                spawn_pos = new BlockPos(spawn_x, height, spawn_z);
                BiomeGenBase biome = spawn_chunk.getBiome(spawn_pos, world.getWorldChunkManager());
                System.out.println("### Place Sandstone at spawn point: " + spawn_pos.toString());
                world.setBlockState(spawn_pos, bs);
                generate_castles(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
                break;
            case -1: //Nether
                System.out.println(" ** NETHER **");
                break;
            case 1: //End
                System.out.println(" ** END **");
                break;
        }
    }

    static int MAX_CASTLES = 1000;
    static int MAX_TRIES = 5;
    static int MIN_SEPARATION = 60; // 150;
    static int CASTLE_SIZE = Castle.GROUNDS_SIZE;
    static int KINGDOM_WIDTH = 800; //2049;
    Castle[] castle_list;

    HashMap<Integer, HashMap<Integer, Castle> > region_castle_map = new HashMap<Integer, HashMap<Integer, Castle>>();

    private int buildings_per_region;
    private int buildings_minimum_separation;

    public CastellaGenerator(int buildings_per_region, int buildings_minimum_separation) {
        castle_list = new Castle[MAX_CASTLES];
        this.buildings_per_region = buildings_per_region;
        this.buildings_minimum_separation = buildings_minimum_separation;
    }

    private void generate_region_castles(Random random, int regionX, int regionZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
        HashMap<Integer, Castle> map;
        if (region_castle_map.containsKey(regionX)) {
            HashMap<Integer, Castle> map = region_castle_map.get(regionX);
            if (map.containsKey(regionZ)) {
                return;
            }
        } else {
            map = new HashMap<Integer, Castle>();
            region_castle_map.put(regionX, map);
        }

    }
    private void build_chunk_castles(Random random, int chunkX, int chunkZ, World world) {

    }
    private void generate_castles(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
        int regionX = chunkX >> 5;
        int regionZ = chunkZ >> 5;

        generate_region_castles(random, regionX, regionZ, world, chunkGenerator, chunkProvider);
        build_chunk_castles(random, chunkX, chunkZ, world);
/*
        int n_castles = 0;
        for (int i = 0; i < MAX_CASTLES; i++) {
            boolean found = false;
            for (int t = 0; !found && t < MAX_TRIES; t++) {
                float x = spawn_point.getX() + (random.nextFloat() * KINGDOM_WIDTH) - (KINGDOM_WIDTH/2);
                float z = spawn_point.getZ() + (random.nextFloat() * KINGDOM_WIDTH) - (KINGDOM_WIDTH/2);
                BlockPos try_pos = new BlockPos(x, 64, z);
                boolean too_close = false;

                for (int p = 0; !too_close && p < MAX_CASTLES && p < n_castles && castle_list[p] != null; p++) {
                    BlockPos castle_pos = castle_list[p].get_position();
                    int dx = (int)Math.floor(x) - castle_pos.getX();
                    int dz = (int)Math.floor(z) - castle_pos.getZ();
                    double dist = Math.sqrt(dx*dx + dz*dz);
                    if (dist < MIN_SEPARATION) {
                        too_close = true;
                    }
                }
                if (too_close) {
                    continue;
                }
                BlockPos bp = Castle.canBuildHere((int)Math.floor(x), (int)Math.floor(z), world, chunkProvider);
                if (bp != null) {
                    castle_list[n_castles] = new Castle(bp);
                    castle_list[n_castles].build(world, chunkProvider);
                    n_castles++;
                    found = true;
                }
            }
        }
        */
    }
    NBTBase CastellaInfo = null;
    private NBTBase getAdditionalInfo(WorldInfo world_info) {
        if (world_info.getAdditionalProperty("CastellaInfo") == null) {
            System.out.println("**CREATE CastellaInfo");
            NBTTagCompound castellainfo = new NBTTagCompound();
            HashMap add_props = new java.util.HashMap<String,net.minecraft.nbt.NBTBase>();
            add_props.put("CastellaInfo", castellainfo);
            world_info.setAdditionalProperties(add_props);
        }
        return world_info.getAdditionalProperty("CastellaInfo");
    }
}
