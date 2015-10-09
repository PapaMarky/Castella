package com.markyshouse.mc.Castella;

import com.markyshouse.mc.MarkyshouseWorldSavedData;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by mark on 9/13/2015.
 */
public class CastellaGenerator  implements IWorldGenerator {
    private int buildings_per_region;
    private int buildings_minimum_separation;
    private int building_max_tries = 5;

    private static boolean working = false;
    HashMap<String, ArrayList<StructureBuilder>> region_castle_map = new HashMap<String, ArrayList<StructureBuilder>>();
    MarkyshouseWorldSavedData markyData = null;

    public CastellaGenerator(int buildings_per_region, int buildings_minimum_separation, int building_max_tries) {
        this.buildings_per_region = buildings_per_region;
        this.buildings_minimum_separation = buildings_minimum_separation;
        this.building_max_tries = building_max_tries;

        // Register the Structure Builders
        StructureFactory.getInstance().register(new TowerBuilder());
        StructureFactory.getInstance().register(new IntersectionBuilder());
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
        if (markyData == null) {
            markyData = MarkyshouseWorldSavedData.forWorld(world);
            markyData.markDirty();
        }
        switch (world.provider.getDimensionId()) {
            case 0: //Overworld
                //System.out.println(" ** OVERWORLD chunk " + chunkX + ", " + chunkZ + " **");
                //NBTTagCompound regionData = markyData.getRegionDataFromChunk(chunkX, chunkZ);
                StructureFactory.getInstance().tryToBuildSomething(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
                break;
            case -1: //Nether
                //System.out.println(" ** NETHER **");
                break;
            case 1: //End
                //System.out.println(" ** END **");
                break;
        }
    }

    private ArrayList<StructureBuilder> get_region_list(int x, int z) {
        String key = String.format("%d:%d", x, z);
        ArrayList<StructureBuilder> list = null;
        if (region_castle_map.containsKey(key)) {
            list = region_castle_map.get(key);
        } else {
            list = new ArrayList<StructureBuilder>();
            region_castle_map.put(key,list);
        }
        return list;
    }

    private void generate_region_castles(Random random, int regionX, int regionZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
        ArrayList<StructureBuilder> castle_list = get_region_list(regionX, regionZ);

        System.out.println("Generate Castles for Region " + regionX + ", " + regionZ);

        // XXX don't need this?
        if (castle_list.size() > 0) {
            System.out.println(" -- Already have them!");
            return;
        }

        // Margin around edge of region to prevent cross region structures.
        // (Is this really needed?)
        int MARGIN = 30;
        int rx = 512 * regionX + MARGIN;
        int rz = 512 * regionZ + MARGIN;
        int rw = 512 - MARGIN - MARGIN;

        int n_structures = 0; // n structures includes times we gave up
        while (n_structures < buildings_per_region) {
            boolean found = false;
            for (int tries = 0; !found && tries < building_max_tries; tries++) {
                float x = rx + (random.nextFloat() * rw);
                float z = rz + (random.nextFloat() * rw);
                BlockPos try_pos = new BlockPos(x, 64, z);
                boolean too_close = false;

                for (StructureBuilder building : castle_list) {
                    /*
                    BlockPos castle_pos = building.getPosition();
                    int dx = (int)Math.floor(x) - castle_pos.getX();
                    int dz = (int)Math.floor(z) - castle_pos.getZ();
                    double dist = Math.sqrt(dx*dx + dz*dz);
                    if (dist < buildings_minimum_separation) {
                        too_close = true;
                        break;
                    }
                    */
                }
                if (too_close) {
                    continue;
                }
                /*
                StructureBuilder b = StructureBuilder.canBuildHere((int) Math.floor(x), (int) Math.floor(z), random, world, chunkProvider);
                if (b != null) {
                    castle_list.add(b);
                    // TODO : Add to persistent data store
                    found = true;
                }
                */
            }
            n_structures++;
        }
    }
    private String CoordsToKey(int x, int z) {
        return String.format("%d:%d", x, z);
    }
    private void build_region_castles(Random random, int regionX, int regionZ, World world, IChunkProvider chunkProvider) {
        ArrayList<StructureBuilder> building_list = get_region_list(regionX, regionZ);
        for (StructureBuilder structureBuilder : building_list) {
            //structureBuilder.build(random, world, chunkProvider);
        }

    }
    HashMap<String, Boolean> seen_chunks = new HashMap<String, Boolean>();
    private void generate_castles(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
        int regionX = chunkX >> 5;
        int regionZ = chunkZ >> 5;
        ArrayList<StructureBuilder> castle_list = get_region_list(regionX, regionZ);

        // TODO use this trick on regions hash table - region_castle_map
        String chunk_key = String.format("%d:%d", chunkX, chunkZ);
        if (seen_chunks.containsKey(chunk_key)) {
            System.out.println(" REPEATED CHUNK: " + chunkX + ", " + chunkZ);
        } else {
            seen_chunks.put(chunk_key, true);
        }

        float chance = random.nextFloat();
        if (chance <= 0.2) {
            System.out.println(String.format(" - Trying to place structure near %d, %d", chunkX, chunkZ));
            boolean found = false;
            for (int tries = 0; !found && tries < building_max_tries; tries++) {
                int x = (chunkX * 16) + (int) (Math.floor((random.nextDouble() * 16.0) - 0.001));
                int z = (chunkZ * 16) + (int) (Math.floor((random.nextDouble() * 16.0) - 0.001));
                BlockPos try_pos = new BlockPos(x, 64, z);
                boolean too_close = false;

                // TODO Should check for other nearby regions as well
                for (StructureBuilder building : castle_list) {
                    /*
                    BlockPos castle_pos = building.getPosition();
                    int dx = (int)Math.floor(x) - castle_pos.getX();
                    int dz = (int)Math.floor(z) - castle_pos.getZ();
                    double dist = Math.sqrt(dx*dx + dz*dz);
                    if (dist < buildings_minimum_separation) {
                        too_close = true;
                        System.out.println(String.format(" -- TOO CLOSE to (%d, %d) - %4f vs %d", castle_pos.getX(), castle_pos.getZ(), dist, buildings_minimum_separation));
                        break;
                    }
                    */
                }
                if (too_close) {
                    continue;
                }
                /*
                StructureBuilder structureBuilder = StructureBuilder.canBuildHere(x, z, random, world, chunkProvider);
                if (structureBuilder != null) {
                    castle_list.add(structureBuilder);
                    // TODO : Add to persistent data store
                    System.out.println(String.format(" -- BINGO!"));
                    structureBuilder.build(random, world, chunkProvider);
                    found = true;
                } else System.out.println(" -- CANNOT BUILD HERE");
                */
            }
        }
        //generate_region_castles(random, regionX, regionZ, world, chunkGenerator, chunkProvider);
        //build_region_castles(random, regionX, regionZ, world, chunkProvider);
    }
}
