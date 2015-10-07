package com.markyshouse.mc.Castella;

import com.markyshouse.mc.IBlockChooser;
import com.markyshouse.mc.StructureBoundingBox2D;
import com.markyshouse.mc.TerrainMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by mark on 10/6/2015.
 */
public class IntersectionBuilder extends StructureBuilder {
    static private char[][] intersection_footprint = {
            { ' ', ' ', ' ', '1', '1', '1', '1', '1', '1', '1', ' ', ' ', ' ' },
            { ' ', '1', '2', '3', '3', '3', '3', '3', '3', '3', '2', '1', ' ' },
            { ' ', '2', '3', '4', '5', '5', '5', '5', '5', '4', '3', '2', ' ' },
            { '1', '3', '4', '6', '7', '7', '7', '7', '7', '6', '4', '3', '1' },
            { '1', '3', '5', '7', '9', '9', '9', '9', '9', '7', '5', '3', '1' },
            { '1', '3', '5', '7', '9', 'g', 'g', 'g', '9', '7', '5', '3', '1' },
            { '1', '3', '5', '7', '9', 'g', 'g', 'g', '9', '7', '5', '3', '1' },
            { '1', '3', '5', '7', '9', 'g', 'g', 'g', '9', '7', '5', '3', '1' },
            { '1', '3', '5', '7', '9', '9', '9', '9', '9', '7', '5', '3', '1' },
            { '1', '3', '4', '6', '7', '7', '7', '7', '7', '6', '4', '3', '1' },
            { ' ', '2', '3', '4', '5', '5', '5', '5', '5', '4', '3', '2', ' ' },
            { ' ', '1', '2', '3', '3', '3', '3', '3', '3', '3', '2', '1', ' ' },
            { ' ', ' ', ' ', '1', '1', '1', '1', '1', '1', '1', ' ', ' ', ' ' },
    };
    public static final List towerSpawnBiomes = Arrays.asList(new BiomeGenBase[]{
            BiomeGenBase.plains, BiomeGenBase.desert, BiomeGenBase.savanna, BiomeGenBase.beach,
            BiomeGenBase.birchForest, BiomeGenBase.birchForestHills, BiomeGenBase.coldBeach, BiomeGenBase.coldTaiga,
            BiomeGenBase.coldTaigaHills, BiomeGenBase.desertHills, BiomeGenBase.extremeHills, BiomeGenBase.extremeHillsEdge,
            BiomeGenBase.forest, BiomeGenBase.forestHills, BiomeGenBase.jungle, BiomeGenBase.jungleEdge, BiomeGenBase.jungleHills,
            BiomeGenBase.mesa, BiomeGenBase.mesaPlateau, BiomeGenBase.roofedForest, BiomeGenBase.savannaPlateau, BiomeGenBase.stoneBeach,
            BiomeGenBase.swampland, BiomeGenBase.taiga, BiomeGenBase.taigaHills
    });
    protected static char[][] getFootprint() { return intersection_footprint; }
    public int getType() { return StructureBuilder.INTERSECTION_TYPE; }
    protected List getBuildableBiomeList() {
        return towerSpawnBiomes;
    }
    void init(Random random) {
        footprint = intersection_footprint;
    }
    public StructureBoundingBox2D getTerritoryBox(BlockPos origin) {
        int area = 32; // this much room on all sides
        return new StructureBoundingBox2D(origin.getX() - area, origin.getZ() - area, origin.getX() + getWidth() + area, origin.getZ() + getHeight() + area);
    }
    public int getWidth() {
        return footprint[0].length;
    }
    public int getHeight() {
        return footprint.length;
    }

    public IntersectionBuilder() {
        footprint = intersection_footprint;
    }
    public void buildInner(Structure structure, BlockPos origin, TerrainMap map, Random random, World world, IChunkProvider chunkProvider) {
        System.out.println(" ##### BUILDING INTERSECTION AT " + origin.getX() + ", " + origin.getY() + ", " + origin.getZ());
        init(random);
        structure.position = new BlockPos(origin.getX() + getWidth()/2, origin.getY(), origin.getZ() + getHeight()/2);
        structure.StructureType = this.getType();

        // TODO - subtype
        IBlockState[][] plantmap = mapGroundCover(origin, random, world, chunkProvider);
        // bulldoze
        IBlockChooser blockChooser = new StoneBrickBlockChooser();
        BullDozer.bullDoze(origin, footprint, blockChooser, blockChooser, random, world, chunkProvider);
        structure.setRoadPoint(EnumFacing.NORTH, structure.position.north(2));
        structure.setRoadPoint(EnumFacing.EAST, structure.position.east(2));
        structure.setRoadPoint(EnumFacing.SOUTH, structure.position.south(2));
        structure.setRoadPoint(EnumFacing.WEST, structure.position.west(2));


        ///// RESTORE GROUND COVER
        // TODO - move to function in StructureBuilder
        for (int x = 0; x < getWidth(); x++) {
            for (int z = 0; z < getHeight(); z++) {
                if (plantmap[x][z] != null) {
                    BlockPos pos = new BlockPos(origin.getX() + x, origin.getY(), origin.getZ() + z);
                    int groundLevel = TerrainMap.getGroundLevel(pos, world, chunkProvider);
                    pos = new BlockPos(origin.getX() + x, groundLevel, origin.getZ() + z);
                    Block groundBlock = world.getBlockState(pos).getBlock();
                    Block coverBlock = plantmap[x][z].getBlock();
                    if (coverBlock.canPlaceBlockAt(world, pos.up())) {
                        world.setBlockState(pos.up(), plantmap[x][z]);
                        if (plantmap[x][z].getBlock() instanceof BlockSapling) {
                            ((BlockSapling)world.getBlockState(pos.up()).getBlock()).generateTree(world, pos.up(), plantmap[x][z], random);
                        }
                    }
                }
            }
        }
        IBlockState bs = Blocks.stonebrick.getDefaultState();
        BlockPos columnPos = new BlockPos(origin.getX() + footprint[0].length/2, origin.getY() + 10, origin.getZ() + footprint.length/2);
        for (int i = 0; i < 50; i++) {
            world.setBlockState(columnPos.up(i + 3), bs);
        }
    }
}