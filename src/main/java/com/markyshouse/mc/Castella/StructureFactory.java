package com.markyshouse.mc.Castella;

import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by mark on 9/22/2015.
 * Duplicate some or all of the functionality of MapGenStructure (net.minecraft.world.gen.structure)
 * but in a generalized way that allows new structures to be added easily.
 * MapGenStructure framework does everything we need, but the built in structures are hardcoded in
 * several places making it impossible to add our own.
 *
 * There are two major parts:
 * 1) generation
 * 2) loading
 *
 * An existing world needs to have information on Structures that have already been generated so
 * that (for example) subsequent structures are not generated too close to existing ones.
 */
public class StructureFactory {
    private static StructureFactory instance = null;
    private final ArrayList<StructureBuilder> structureBuilders = new ArrayList<StructureBuilder>();

    public static synchronized StructureFactory getInstance() {
        if (instance == null) {
            instance = new StructureFactory();
        }
        return instance;
    }

    public synchronized void register(final StructureBuilder structureBuilder) {
        structureBuilders.add(structureBuilder);
    }

    private boolean trying_to_build = false;
    public synchronized Structure tryToBuildSomething(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
        if (trying_to_build) return null;
        trying_to_build = true;
        // Randomly choose structure builders(s). (right now we only have one)
        if (structureBuilders.size() <= 0) return null;
        StructureBuilder builder = null;

        // TODO - this is ugly. Make the register function take a weight value
        int index = 1; // random.nextInt(structureBuilders.size());
        float f = random.nextFloat();
        if(f < 0.25) {
            index = 0;
        }
        builder = structureBuilders.get(index);

        Structure s = builder.tryToBuildHere(chunkX * 16, chunkZ * 16, random, world, chunkProvider);
        trying_to_build = false;
        return s;
    }
}
