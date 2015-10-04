package com.markyshouse.mc.Castella;

import com.markyshouse.mc.IBlockChooser;
import com.markyshouse.mc.StructureBoundingBox2D;
import com.markyshouse.mc.TerrainMap;
import net.minecraft.block.*;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemDoor;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.border.IBorderListener;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.IPlantable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by mark on 9/16/2015.
 */
public class TowerBuilder extends StructureBuilder {
    public enum TYPE {STONE}

    static private char[][] tower_footprint = {
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', '0', '0', '0', '1', '0', '0', '0', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', '0', '1', '2', '2', '2', '3', '2', '2', '2', '1', '0', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', '0', '1', '2', '3', '4', '4', '4', '5', '4', '4', '4', '3', '2', '1', '0', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', '0', '2', '3', '4', '5', '6', '6', '6', '7', '6', '6', '6', '5', '4', '3', '2', '0', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', '0', '2', '3', '5', '6', '7', '7', '8', '8', '9', '8', '8', '7', '7', '6', '5', '3', '2', '0', ' ', ' ', ' '},
            {' ', ' ', '0', '2', '3', '5', '6', '7', '8', '9', 'g', 'g', 'g', 'g', 'g', '9', '8', '7', '6', '5', '3', '2', '0', ' ', ' '},
            {' ', ' ', '1', '3', '5', '6', '8', '9', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', '9', '8', '6', '5', '3', '1', ' ', ' '},
            {' ', '0', '2', '4', '6', '7', '9', 'g', 'g', 'g', 'g', 'W', 'W', 'W', 'g', 'g', 'g', 'g', '9', '7', '6', '4', '2', '0', ' '},
            {' ', '1', '3', '5', '7', '8', 'g', 'g', 'g', 'W', 'W', 'F', 'F', 'F', 'W', 'W', 'g', 'g', 'g', '8', '7', '5', '3', '1', ' '},
            {'0', '2', '4', '6', '7', '9', 'g', 'g', 'W', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'W', 'g', 'g', '9', '7', '6', '4', '2', '0'},
            {'0', '2', '4', '6', '8', 'g', 'g', 'g', 'W', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'W', 'g', 'g', 'g', '8', '6', '4', '2', '0'},
            {'0', '2', '4', '6', '8', 'g', 'g', 'W', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'W', 'g', 'g', '8', '6', '4', '2', '0'},
            {'1', '3', '5', '7', '9', 'g', 'g', 'D', 'F', 'F', 'F', 'F', 'c', 'F', 'F', 'F', 'F', 'D', 'g', 'g', '9', '7', '5', '3', '1'},
            {'0', '2', '4', '6', '8', 'g', 'g', 'W', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'W', 'g', 'g', '8', '6', '4', '2', '0'},
            {'0', '2', '4', '6', '8', 'g', 'g', 'g', 'W', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'W', 'g', 'g', 'g', '8', '6', '4', '2', '0'},
            {'0', '2', '4', '6', '7', '9', 'g', 'g', 'W', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'W', 'g', 'g', '9', '7', '6', '4', '2', '0'},
            {' ', '1', '3', '5', '7', '8', 'g', 'g', 'g', 'W', 'W', 'F', 'F', 'F', 'W', 'W', 'g', 'g', 'g', '8', '7', '5', '3', '1', ' '},
            {' ', '0', '2', '4', '6', '7', '9', 'g', 'g', 'g', 'g', 'W', 'W', 'W', 'g', 'g', 'g', 'g', '9', '7', '6', '4', '2', '0', ' '},
            {' ', ' ', '1', '3', '5', '6', '8', '9', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', '9', '8', '6', '5', '3', '1', ' ', ' '},
            {' ', ' ', '0', '2', '3', '5', '6', '7', '8', '9', 'g', 'g', 'g', 'g', 'g', '9', '8', '7', '6', '5', '3', '2', '0', ' ', ' '},
            {' ', ' ', ' ', '0', '2', '3', '5', '6', '7', '7', '8', '8', '9', '8', '8', '7', '7', '6', '5', '3', '2', '0', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', '0', '2', '3', '4', '5', '6', '6', '6', '7', '6', '6', '6', '5', '4', '3', '2', '0', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', '0', '1', '2', '3', '4', '4', '4', '5', '4', '4', '4', '3', '2', '1', '0', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', '0', '1', '2', '2', '2', '3', '2', '2', '2', '1', '0', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', '0', '0', '0', '1', '0', '0', '0', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '}
        };

    boolean hasDoors = true;
    protected static char[][] getFootprint() { return tower_footprint; }
    public static List towerSpawnBiomes = Arrays.asList(new BiomeGenBase[]{
            BiomeGenBase.plains, BiomeGenBase.desert, BiomeGenBase.savanna, BiomeGenBase.beach,
            BiomeGenBase.birchForest, BiomeGenBase.birchForestHills, BiomeGenBase.coldBeach, BiomeGenBase.coldTaiga,
            BiomeGenBase.coldTaigaHills, BiomeGenBase.desertHills, BiomeGenBase.extremeHills, BiomeGenBase.extremeHillsEdge,
            BiomeGenBase.forest, BiomeGenBase.forestHills, BiomeGenBase.jungle, BiomeGenBase.jungleEdge, BiomeGenBase.jungleHills,
            BiomeGenBase.mesa, BiomeGenBase.mesaPlateau, BiomeGenBase.roofedForest, BiomeGenBase.savannaPlateau, BiomeGenBase.stoneBeach,
            BiomeGenBase.swampland, BiomeGenBase.taiga, BiomeGenBase.taigaHills
    });

    ArrayList<Integer[]> walls = new ArrayList<Integer[]>();
    ArrayList<Integer[]> floors = new ArrayList<Integer[]>();
    ArrayList<Integer[]> grounds = new ArrayList<Integer[]>();
    ArrayList<Integer[]> slope = new ArrayList<Integer[]>();

    Integer[] center = null;
    EnumFacing ladder_facing;

    BlockPos bridge_road_point = null;

    protected List getBuildableBiomeList() {
        return towerSpawnBiomes;
    }
    public StructureBoundingBox2D getTerritoryBox(BlockPos origin) {
        int area = 64; // this much room on all sides
        return new StructureBoundingBox2D(origin.getX() - area, origin.getZ() - area, origin.getX() + getWidth() + area, origin.getZ() + getHeight() + area);
    }
    public int getType() { return StructureBuilder.TOWER_TYPE; }

    protected void addWalls(BlockPos position, int floor, IBlockChooser blockChooser, World world, IChunkProvider chunkProvider) {
        for (Integer[] point : walls) {
            BlockPos p = position.east(point[0]).south(point[1]).up(1 + floor * 3);
            for (int y = 0; y < 3; y++) {
                world.destroyBlock(p.up(y), false);
                world.setBlockState(p.up(y), blockChooser.chooseBlock(p.up(y), world, chunkProvider));
            }
        }
    }

    protected void addCeiling(BlockPos position, int floor, IBlockChooser blockChooser, World world, IChunkProvider chunkProvider) {
        for (Integer[] point : floors) {
            BlockPos p = position.east(point[0]).south(point[1]).up(1 + floor * 3);
            world.destroyBlock(p.up(2), false);
            world.setBlockState(p.up(2), blockChooser.chooseBlock(p.up(2), world, chunkProvider));
        }
    }

    protected void addLadder(BlockPos position, int floor, IBlockChooser blockChooser, World world, IChunkProvider chunkProvider) {
        BlockPos center_pos = position.east(center[0]).south(center[1]).up();
        BlockPos ladder_pos = center_pos;
        if (ladder_facing == EnumFacing.EAST) {
            ladder_pos = center_pos.east();
        }
        if (ladder_facing == EnumFacing.WEST) {
            ladder_pos = center_pos.west();
        }
        if (ladder_facing == EnumFacing.SOUTH) {
            ladder_pos = center_pos.south();
        }
        if (ladder_facing == EnumFacing.NORTH) {
            ladder_pos = center_pos.north();
        }
        for (int y = 0; y < 3; y++) {
            int yoff = y + (floor * 3);
            world.destroyBlock(center_pos.up(yoff), false);
            world.setBlockState(center_pos.up(yoff), wallChooser.chooseBlock(center_pos.up(yoff), world, chunkProvider));
            world.destroyBlock(ladder_pos.up(yoff), false);
            IBlockState ladder_state = Blocks.ladder.getDefaultState().withProperty(BlockLadder.FACING, ladder_facing);
            world.setBlockState(ladder_pos.up(yoff), ladder_state);
        }
    }

    protected EnumFacing addUpperDoor(BlockPos position, Random random, int floor, IBlockChooser blockChooser, World world, IChunkProvider chunkProvider) {

        // Look for a place to build a bridge to.
        BlockPos floor_center = new BlockPos(position.getX() + getWidth()/2, position.getY() + (floor * 3), position.getZ() + getHeight()/2);

        EnumFacing bridge_facing = null;

        int range = 12 + 5; // 12 is half footprint, 5 is how far beyond to check.
        int ledge = 6; // 6 is how far to the outside of the tower.
        // try North
        for (int i = ledge; i < range; i++) {
            if (TerrainMap.isGround(world.getBlockState(floor_center.north(i)).getBlock())) {
                bridge_facing = EnumFacing.NORTH;
                break;
            }
            if (TerrainMap.isGround(world.getBlockState(floor_center.east(i)).getBlock())) {
                bridge_facing = EnumFacing.EAST;
                break;
            }
            if (TerrainMap.isGround(world.getBlockState(floor_center.south(i)).getBlock())) {
                bridge_facing = EnumFacing.SOUTH;
                break;
            }
            if (TerrainMap.isGround(world.getBlockState(floor_center.west(i)).getBlock())) {
                bridge_facing = EnumFacing.WEST;
                break;
            }
        }

        if (bridge_facing == null) return null;
        BlockPos door_position = null;
        // build the bridge
        for (int i = ledge; i < range; i++) {
            BlockPos p0 = null;
            BlockPos p1 = null;
            BlockPos p2 = null;
            switch (bridge_facing) {
                case EAST:
                    p0 = floor_center.east(i).north(1);
                    p1 = floor_center.east(i);
                    p2 = floor_center.east(i).south(1);
                    break;
                case WEST:
                    p0 = floor_center.west(i).north(1);
                    p1 = floor_center.west(i);
                    p2 = floor_center.west(i).south(1);
                    break;
                case NORTH:
                    p0 = floor_center.north(i).east(1);
                    p1 = floor_center.north(i);
                    p2 = floor_center.north(i).west(1);
                    break;
                case SOUTH:
                    p0 = floor_center.south(i).east(1);
                    p1 = floor_center.south(i);
                    p2 = floor_center.south(i).west(1);
                    break;
            }
            if (!TerrainMap.isGround(world.getBlockState(p0).getBlock())
                    || !TerrainMap.isGround(world.getBlockState(p1).getBlock())
                    || !TerrainMap.isGround(world.getBlockState(p2).getBlock())) {
                for (int k = 0; k <= 3; k++) {
                    world.destroyBlock(p0.up(k), false);
                    world.destroyBlock(p1.up(k), false);
                    world.destroyBlock(p2.up(k), false);
                }
                world.setBlockState(p0, floorChooser.chooseBlock(p0,world,chunkProvider));
                world.setBlockState(p1, floorChooser.chooseBlock(p0,world,chunkProvider));
                        // Blocks.stone_slab2.getDefaultState().withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.TOP).withProperty(BlockStoneSlabNew.VARIANT, BlockStoneSlabNew.EnumType.RED_SANDSTONE));
                world.setBlockState(p2, floorChooser.chooseBlock(p2, world, chunkProvider));
            } else {
                for (int k = 1; k <= 3; k++) {
                    world.destroyBlock(p0.up(k), false);
                    world.destroyBlock(p1.up(k), false);
                    world.destroyBlock(p2.up(k), false);
                }
                bridge_road_point = p1;
                break;
            }
        }

        // Create the door opening
        switch(bridge_facing) {
            case EAST: door_position = floor_center.east(ledge - 1).up(); break;
            case WEST: door_position = floor_center.west(ledge - 1).up(); break;
            case SOUTH: door_position = floor_center.south(ledge - 1).up(); break;
            case NORTH: door_position = floor_center.north(ledge - 1).up(); break;
        }
        world.destroyBlock(door_position, false);
        world.destroyBlock(door_position.up(), false);

        EnumFacing door_facing = bridge_facing.getOpposite();
        // one in one hundred don't have a solid door
        if (random.nextFloat() < 0.01) return bridge_facing;

        Block door = doorChooser.chooseBlock(door_position, world, chunkProvider).getBlock();
        ItemDoor.placeDoor(world, door_position, door_facing, door);

        return bridge_facing;
    }

    protected void addDoor(BlockPos position, Random random, int floor, IBlockChooser blockChooser, World world, IChunkProvider chunkProvider) {
        if (random.nextFloat() < 0.01) return;
        BlockPos floor_center = new BlockPos(position.getX() + getWidth()/2, position.getY() + (floor * 3), position.getZ() + getHeight()/2);
        int ledge = 6; // 6 is how far to the outside of the tower.

        EnumFacing door_facing = EnumFacing.random(random);
        while (door_facing == EnumFacing.DOWN || door_facing == EnumFacing.UP)
            door_facing = EnumFacing.random(random);

        BlockPos door_position = null;
        switch(door_facing) {
            case EAST: door_position = floor_center.east(ledge - 1).up(); break;
            case WEST: door_position = floor_center.west(ledge - 1).up(); break;
            case SOUTH: door_position = floor_center.south(ledge - 1).up(); break;
            case NORTH: door_position = floor_center.north(ledge - 1).up(); break;
        }
        world.destroyBlock(door_position, false);
        world.destroyBlock(door_position.up(), false);

        door_facing = door_facing.getOpposite();
        // one in one hundred don't have a solid door
        if (random.nextFloat() < 0.15) return;

        Block door = doorChooser.chooseBlock(door_position, world, chunkProvider).getBlock();
        ItemDoor.placeDoor(world, door_position, door_facing, door);
    }

    enum WindowPattern {SINGLE_CENTER};

    public void makeWindow(BlockPos position, IBlockState state, World world) {
        world.destroyBlock(position, false);
        world.destroyBlock(position.up(), false);
        if (state != null) {
            world.setBlockState(position, state);
            world.setBlockState(position.up(), state);
        }
    }
    public void addWindows(BlockPos position, EnumFacing side_used, Random random, int floor, IBlockChooser blockChooser, World world, IChunkProvider chunkProvider) {
        BlockPos floor_center = new BlockPos(position.getX() + getWidth()/2, position.getY() + (floor * 3), position.getZ() + getHeight()/2);
        int edge = 5; // 6 is how far to the outside of the tower.

        if (side_used != EnumFacing.WEST) {
            BlockPos p = floor_center.west(edge).up();
            IBlockState window_state = blockChooser == null ? null : blockChooser.chooseBlock(p, world, chunkProvider);
            makeWindow(p, window_state, world);
        }

        if (side_used != EnumFacing.EAST) {
            BlockPos p = floor_center.east(edge).up();
            IBlockState window_state = blockChooser == null ? null : blockChooser.chooseBlock(p, world, chunkProvider);
            makeWindow(p, window_state, world);
        }
        if (side_used != EnumFacing.NORTH) {
            BlockPos p = floor_center.north(edge).up();
            IBlockState window_state = blockChooser == null ? null : blockChooser.chooseBlock(p, world, chunkProvider);
            makeWindow(p, window_state, world);
        }
        if (side_used != EnumFacing.SOUTH) {
            BlockPos p = floor_center.south(edge).up();
            IBlockState window_state = blockChooser == null ? null : blockChooser.chooseBlock(p, world, chunkProvider);
            makeWindow(p, window_state, world);
        }
    }

    public void addFloor(BlockPos position, Random random, int floor, IBlockChooser blockChooser, World world, IChunkProvider chunkProvider) {
        addWalls(position, floor, wallChooser, world, chunkProvider);
        addCeiling(position, floor, floorChooser, world, chunkProvider);
        addLadder(position, floor, floorChooser, world, chunkProvider);
        if (floor == 0)
            addDoor(position, random, floor, doorChooser, world, chunkProvider);

        EnumFacing side_used = null;
        if (floor == 2)
            side_used = addUpperDoor(position, random, floor, doorChooser, world, chunkProvider);

        if (floor > 0)
            addWindows(position, side_used, random, floor, windowChooser, world, chunkProvider);

        // ladder vs spiral stair case
        ladder_facing = ladder_facing.rotateY();
    }
    public int getWidth() {
        return footprint[0].length;
    }
    public int getHeight() {
        return footprint.length;
    }

    public void init(Random random) {

        // TODO needs to move to StructureTower
        n_floors = 3 + (int)Math.floor(random.nextFloat() * 5.0);
        if (random.nextFloat() < 0.25)
            hasDoors = false;

        ladder_facing = EnumFacing.random(random);
        while (ladder_facing == EnumFacing.DOWN || ladder_facing == EnumFacing.UP)
            ladder_facing = EnumFacing.random(random);

        bridge_road_point = null;
    }

    protected  IBlockState[][] mapGroundCover(BlockPos origin, Random random, World world, IChunkProvider chunkProvider) {
        int width = getWidth();
        int height = getHeight();

        IBlockState[][] plantmap = new IBlockState[width][height];
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < height; z++) {
                if (footprint[x][z] == ' ') continue;

                BlockPos p = new BlockPos(origin.getX() + x, 64, origin.getZ() + z);
                Chunk chunk = chunkProvider.provideChunk(p);
                int h = chunk.getHeight(p);
                p = new BlockPos(p.getX(), h, p.getZ());
                Block block = chunk.getBlock(p);
                while (!TerrainMap.isGround(block)) {
                    p = p.down();
                    block = chunk.getBlock(p);
                }
                int ground_level = p.getY();
                Block cover = chunk.getBlock(p.up());
                if (cover instanceof IPlantable) {
                    plantmap[x][z] = world.getBlockState(p.up()); // cover.getActualState(cover.getDefaultState(), world, p.up());
                } else if (cover instanceof BlockLog) {
                    IBlockState cover_bs = cover.getActualState(cover.getDefaultState(), world, p.up());
                    IBlockState sapling_bs = null;

                    if (cover instanceof BlockNewLog)
                        sapling_bs = Blocks.sapling.getDefaultState().withProperty(BlockSapling.TYPE, cover_bs.getValue(BlockNewLog.VARIANT));
                    else
                        sapling_bs = Blocks.sapling.getDefaultState().withProperty(BlockSapling.TYPE, cover_bs.getValue(BlockOldLog.VARIANT));
                    sapling_bs = sapling_bs.withProperty(BlockSapling.STAGE, Integer.valueOf(1));
                    plantmap[x][z] = sapling_bs;
                }

                // now destroy everything between ground_level & h
                while (h > ground_level) {
                    BlockPos pos = new BlockPos(p.getX(), h, p.getZ());
                    Block block1 = chunk.getBlock(pos);
                    if (block1 instanceof  BlockLeaves || block1 instanceof BlockLog) {
                        block1.breakBlock(world, pos, block1.getDefaultState());}
                    if ((footprint[x][z] < '0' && footprint[x][z] > '9') || block1 instanceof IPlantable || block1 instanceof BlockLog)
                        world.destroyBlock(pos, false);
                    h--;
                }
            }
        }
        return plantmap;
    }
    public void buildInner(Structure structure, BlockPos position, TerrainMap map, Random random, World world, IChunkProvider chunkProvider) {
        System.out.println(" ##### BUILDING TOWER AT " + position.getX() + ", " + position.getY() + ", " + position.getZ());
        init(random);
        structure.position = new BlockPos(position.getX() + getWidth()/2, position.getY(), position.getZ() + getHeight()/2);
        structure.StructureType = StructureBuilder.TOWER_TYPE;
            /*
            switch (type) {
                case STONE:
                    setupStoneTower();
            }
            */
        setupStoneTower(random); // only one we have now
        BullDozer.ACTION action = BullDozer.ACTION.IGNORE;
        IBlockChooser blockChooser = new StoneBrickBlockChooser();

        // Step 1: clear land and map plants & trees
        //         - add plants to map
        //         - convert trees to saplings, add to map
        //         - pass 1: destroy all plants & wood.
        //         - pass 2: expand square by 10, destroy all decayable leaves
        // Step 2: Bulldoze
        // Step 3: Build Structure
        // Step 4: Replant plants & trees

        IBlockState[][] plantmap = mapGroundCover(position, random, world, chunkProvider);

        // bulldoze
        BullDozer.bullDoze(position, footprint, blockChooser, blockChooser, random, world, chunkProvider);

        // build 1st floor
        for (int f = 0; f < n_floors; f++) {
            addFloor(position, random, f, blockChooser, world, chunkProvider);
        }
        structure.addRoadPoint(position.east(8));
        structure.addRoadPoint(position.west(8));
        structure.addRoadPoint(position.north(8));
        structure.addRoadPoint(position.south(8));
        if (bridge_road_point != null) {
            structure.addRoadPoint(bridge_road_point);
        }

        ///// RESTORE GROUND COVER
        for (int x = 0; x < getWidth(); x++) {
            for (int z = 0; z < getHeight(); z++) {
                if (plantmap[x][z] != null) {
                    BlockPos pos = new BlockPos(position.getX() + x, position.getY(), position.getZ() + z);
                    int groundLevel = TerrainMap.getGroundLevel(pos, world, chunkProvider);
                    pos = new BlockPos(position.getX() + x, groundLevel, position.getZ() + z);
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

        // mark the tower with a pillar
        // IBlockState bs = Blocks.stonebrick.getDefaultState();
        /*
        int base = (n_floors + 1) * 3 + 3;
        BlockPos columnPos = new BlockPos(position.getX() + footprint[0].length/2, position.getY() + 10, position.getZ() + footprint.length/2);
        for (int i = 0; i < 50; i++) {
            world.setBlockState(columnPos.up(i + n_floors * 3 + 3), bs);
        }
        */
        // ...
    }
    public void buildGroundFloor(Random random, int x, int z, int h, int floor, World world, IChunkProvider chunkProvider) {
    }
    int n_floors = 0;

    public TowerBuilder() {
        footprint = getFootprint();

        for (int x = 0; x < getWidth(); x++)
            for (int z = 0; z < getHeight(); z++) {
                Integer[] point = new Integer[]{x, z};

                switch(footprint[x][z]) {
                    case 'W':
                        walls.add(point);
                        slope.add( new Integer[]{x, z, 10} );
                        break;
                    case 'F':
                        floors.add(point);
                        slope.add( new Integer[]{x, z, 10} );
                        break;
                    case 'D':
                        walls.add(point);
                        slope.add( new Integer[]{x, z, 10} );
                        break;
                    case 'c':
                        floors.add(point);
                        center = point;
                        slope.add( new Integer[]{x, z, 10} );
                        break;
                    case 'g':
                        grounds.add(point);
                        slope.add( new Integer[]{x, z, 10} );
                        break;
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        slope.add( new Integer[]{x, z, (int)(footprint[x][z])} );
                        break;
                }
            }
    }

    private void setupStoneTower(Random random) {
        StoneBrickBlockChooser stoneBrickBlockChooser = new StoneBrickBlockChooser();
        SimpleBlockChooser oakDoorChooser = new SimpleBlockChooser(Blocks.oak_door.getDefaultState());
        IBlockState blockState = Blocks.wooden_slab.getDefaultState().withProperty(BlockWoodSlab.VARIANT,BlockPlanks.EnumType.OAK);
        blockState = blockState.withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.TOP);
        IBlockChooser window_chooser = null;
        if (random.nextFloat() > 0.1) {
            IBlockState windows =
                    random.nextFloat() > 0.25 ? Blocks.glass_pane.getDefaultState() : Blocks.iron_bars.getDefaultState();
            window_chooser = new SimpleBlockChooser(windows);
        }

        SimpleBlockChooser oakSlabChooser = new SimpleBlockChooser(blockState);
        setSlopeChooser(stoneBrickBlockChooser);
        setGroundsChooser(stoneBrickBlockChooser);
        setFloorChooser(oakSlabChooser);
        setDoorChooser(oakDoorChooser);
        setWallChooser(stoneBrickBlockChooser);
        setWindowChooser(window_chooser);
    }

    // define kinds of levels (floorplans) and stack them up randomly
    class Level {
        private int floor_h = 0; // world coordinates
        TowerBuilder parent = null;

        public Level(TowerBuilder p, int h) {
            parent = p;
            floor_h = h;
        }

        public int getHeight() {
            return 0;
        }

        public void build(Random random, int x, int z, World world, IChunkProvider chunkProvider) {

        }
    }
}
