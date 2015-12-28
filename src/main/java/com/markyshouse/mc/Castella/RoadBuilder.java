package com.markyshouse.mc.Castella;

import com.markyshouse.mc.MarkyshouseWorldSavedData;
import com.markyshouse.mc.TerrainMap;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemSign;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.village.Village;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.structure.MapGenStructureData;
import net.minecraft.world.storage.MapStorage;
import org.lwjgl.Sys;

import java.util.*;

/**
 * Created by mark on 10/2/2015.
 */
public class RoadBuilder {

    static class TempleBox {
        int[] bb = null;
    }
    static class VillageBox {
        BlockPos center;
        int radius;
    }
    static private ArrayList<VillageBox> villageBoxes = new ArrayList<VillageBox>();
    static private ArrayList<TempleBox> templeBoxes = new ArrayList<TempleBox>();

    static boolean haveTempleBox(int[] bb) {
        Iterator<TempleBox> tit = templeBoxes.iterator();
        while (tit.hasNext()) {
            TempleBox tbox = tit.next();
            if (bb[0] == tbox.bb[0] && bb[2] == tbox.bb[2] && bb[3] == tbox.bb[3] && bb[5] == tbox.bb[5]) {
                return true;
            }
        }
        return false;
    }

    static boolean haveCenterInVillageBoxes(BlockPos center) {
        Iterator<TempleBox> tit = templeBoxes.iterator();
        int bx = center.getX();
        int bz = center.getZ();
        while (tit.hasNext()) {
            TempleBox tb = tit.next();
            if (bx >= tb.bb[0] && bx <= tb.bb[3] && bz >= tb.bb[2] && bz <= tb.bb[5]) {
                return true;
            }
        }
        Iterator<VillageBox> vit = villageBoxes.iterator();
        while (vit.hasNext()) {
            VillageBox vbox = vit.next();
            if (vbox.center == center) return true;
        }

        return false;
    }
    static public void updateVillageBoxes(World world) {
        MapStorage storage = world.getPerWorldStorage();
        VillageCollection villageCollection = (VillageCollection)storage.loadData(MarkyshouseWorldSavedData.class, "villages");

        List villageList = villageCollection.getVillageList();
        Iterator iterator = villageList.iterator();

        while (iterator.hasNext()) {
            Village village1 = (Village) iterator.next();
            // do we have this one?
            if (! haveCenterInVillageBoxes(village1.getCenter())) {
                VillageBox vb = new VillageBox();
                vb.center = village1.getCenter();
                vb.radius = village1.getVillageRadius();
                villageBoxes.add(vb);
                System.out.println(String.format("FOUND NEW VILLAGE: count is: %d", villageBoxes.size()));
                Iterator<VillageBox> vbit = villageBoxes.iterator();
                while (vbit.hasNext()) {
                    VillageBox box = vbit.next();
                    System.out.println(String.format(" - center: %s, radius: %d", box.center.toString(), box.radius));
                }
            }
        }
        MapGenStructureData temples = (MapGenStructureData)storage.loadData(MapGenStructureData.class, "Temple");
        NBTTagCompound tmpls = temples.func_143041_a();
        Iterator it = tmpls.getKeySet().iterator();
        while (it.hasNext()) {
            String k = (String) it.next();
            NBTTagCompound t = tmpls.getCompoundTag(k);
            int[] tbb = t.getIntArray("BB");
            if ( ! haveTempleBox(tbb)) {
                TempleBox tb = new TempleBox();
                tb.bb = tbb;
                templeBoxes.add(tb);
                System.out.println(String.format("FOUND NEW Temple: count is: %d", templeBoxes.size()));
                Iterator<TempleBox> tbit = templeBoxes.iterator();
                while (tbit.hasNext()) {
                    TempleBox box = tbit.next();
                    System.out.println(String.format(" - box: (%d, %d) - (%d, %d)", box.bb[0], box.bb[2], box.bb[3], box.bb[5]));
                }
            }
        }
    }

    static private boolean positionIsInVillege(BlockPos blockPos) {
        Iterator<VillageBox> vit = villageBoxes.iterator();
        while (vit.hasNext()) {
            VillageBox vbox = vit.next();
            double r = vbox.radius * 2;
            double radiusSquared = r * r;
            double dx = (double)blockPos.getX() - vbox.center.getX();
            double dz = (double)blockPos.getZ() - vbox.center.getZ();

            double distanceSquared = dx * dx + dz * dz;
            if (distanceSquared < radiusSquared) {
                return true;
            }
        }

        return false;
    }

    public class RoadSegment {
        int p0;
        int p1;
        double length;
        int rise;

        public RoadSegment(int pos0, int pos1, double length, int rise) {
            p0 = pos0;
            p1 = pos1;
            this.length = length;
            this.rise = rise;
        }
    }

    private ArrayList<BlockPos> point_list;
    protected Structure structure0;
    protected Structure structure1;
    private double MAX_SEGMENT_LENGTH = 25;

    // mainly intended to identify blocks that are already parts of roads
    // not 100% accurate
    public static boolean isNatural(BlockPos blockPos, World world) {
        Block block = world.getBlockState(blockPos).getBlock();
        if (block instanceof BlockPlanks || block instanceof BlockGlass ||
                block instanceof BlockCompressed || block instanceof BlockSlab ||
                block instanceof BlockStairs || block instanceof BlockDispenser ||
                block instanceof BlockRailBase || block instanceof BlockContainer ||
                block instanceof BlockPressurePlate || block instanceof BlockDoor ||
                block instanceof BlockLadder || block instanceof BlockStoneBrick ||
                block == Blocks.brick_block)
            return false;

        return true;
    }
    public BlockPos getCenter(BlockPos p0, BlockPos p1) {
        int x = p0.getX() + (p1.getX() - p0.getX())/2;
        int y = p0.getY() + (p1.getY() - p0.getY())/2;
        int z = p0.getZ() + (p1.getZ() - p0.getZ())/2;
        return new BlockPos(x, y, z);
    }

    public RoadBuilder() {
    }

    private void plot(BlockPos blockPos, IBlockState blockState, World world, IChunkProvider chunkProvider) {
        if (!isNatural(blockPos, world)) {
            return;
        }
        if (positionIsInVillege(blockPos)) return;

        Chunk chunk = chunkProvider.provideChunk(blockPos);
        int h = chunk.getHeight(blockPos);
        int n = 0;
        final int TUNNEL_HEIGHT = 3;
        BlockPos pos = blockPos.up();
        while (TerrainMap.isGround(chunk.getBlock(pos))) {
            pos = pos.up();
        }

        int hh = pos.getY();
        while (hh < h && n < TUNNEL_HEIGHT) {
            if (TerrainMap.isGround(chunk.getBlock(pos))) {
                n++;
            }
            pos = pos.up();
            hh = pos.getY();
        }
        if (hh < h) {
            // we're building a tunnel
            if (!BullDozer.destroyTree(pos, world, chunkProvider)) {
                world.destroyBlock(pos, false);
            }
            world.setBlockState(pos, Blocks.brick_block.getDefaultState());
            hh--;
        }
        while (hh > blockPos.getY()) {
            pos = new BlockPos(blockPos.getX(), hh, blockPos.getZ());
            Chunk chunk1 = chunkProvider.provideChunk(pos);
            Block block = chunk1.getBlock(pos);
            if (!(block instanceof BlockLeavesBase) && !BullDozer.destroyTree(pos, world, chunkProvider)) {
                world.destroyBlock(pos, false);
            }
            hh--;
        }
        Block block2 = chunkProvider.provideChunk(blockPos.up()).getBlock(blockPos.up());
        while (TerrainMap.isLiquid(block2)) {
            blockPos = blockPos.up();
            block2 = chunkProvider.provideChunk(blockPos.up()).getBlock(blockPos.up());
        }
        //System.out.println(String.format("   -- plot %s", block2.toString()));
        world.setBlockState(blockPos, blockState);
        /*
        for (int i = 1; i < 5; i++)
            world.setBlockState(blockPos.up(i), Blocks.glass.getDefaultState());
            */
    }

    private boolean compare(int i, int imax, int incr) {
        if (incr > 0) {
            return i < imax;
        }
        return i > imax;
    }

    private BlockPos calculate_height(BlockPos b, BlockPos start, BlockPos end, double segment_length) {
        double dx = b.getX() - start.getX();
        double dz = b.getZ() - start.getZ();
        double distance = Math.sqrt(dx*dx + dz*dz);
        double pct = distance / segment_length;
        double h = start.getY() + pct * (end.getY() - start.getY());
        return new BlockPos(b.getX(), Math.round(h), b.getZ());
    }

    public enum BlockSlope {
        UP, FLAT, DOWN
    }
    public class RoadBlock {
        public BlockPos pos;
        public int left;
        public int right;
        public BiomeGenBase biome;
        public EnumFacing direction;
        public int waterLevel;
        public int groundLevel;
        public int roadLevel;
        public BlockSlope slope;
    }

    ArrayList<RoadBlock> blockList = new ArrayList<RoadBlock>();

    private RoadBlock addRoadBlock(BlockPos pos, EnumFacing direction, World world, IChunkProvider chunkProvider) {
        Chunk chunk = chunkProvider.provideChunk(pos);
        RoadBlock block = new RoadBlock();
        block.pos = pos;
        block.left = 1;
        block.right = 1;
        block.direction = direction;
        block.biome = world.getBiomeGenForCoords(pos);
        block.waterLevel = -1;
        block.groundLevel = -1;
        block.roadLevel = pos.getY();
        block.slope = BlockSlope.FLAT;

        int h = chunk.getHeight(pos);
        pos = new BlockPos(pos.getX(), h, pos.getZ());
        while(true) {
            if (block.waterLevel == -1 && TerrainMap.isLiquid(chunk.getBlock(pos))) {
                block.waterLevel = pos.getY();
            } else if (block.groundLevel == -1 && TerrainMap.isGround(chunk.getBlock(pos))) {
                block.groundLevel = pos.getY();
                break;
            }
            pos = pos.down();
        }
        blockList.add(block);
        return block;
    }

    private void renderNorthSouthSegment(BlockPos p0, BlockPos p1,
                                         EnumFacing direction, double segment_length, boolean last_segment,
                                         World world, IChunkProvider chunkProvider) {
        double deltax = p1.getX() - p0.getX();
        double deltaz = p1.getZ() - p0.getZ();
        int zInc = deltaz < 0 ? -1 : 1;
        int xInc = deltax < 0 ? -1 : 1;
        double error = 0;
        double deltaerr = Math.abs(deltax / deltaz);
        int z0 = p0.getZ();
        int x = p0.getX();
        int z1 = p1.getZ();
        for (int z = z0; compare(z, z1, zInc); z += zInc) {
            BlockPos pos = calculate_height(new BlockPos(x, 64, z), p0, p1, segment_length);
            RoadBlock block = addRoadBlock(pos, direction, world, chunkProvider);

            error = error + deltaerr;
            while (error >= 0.5) {
                x = x + xInc;
                error = error - 1.0;
                if (xInc > 0) {
                    // facing north, east is right
                    if (direction == EnumFacing.NORTH)
                        block.right++;
                    else
                        block.left++;
                } else {
                    if (direction == EnumFacing.NORTH)
                        block.left++;
                    else
                        block.right++;
                }
            }
        }
        if (last_segment) {
            BlockPos pos = calculate_height(p1, p0, p1, segment_length);
            addRoadBlock(pos, direction, world, chunkProvider);
        }
        world.setBlockState(p0.up(4), Blocks.lit_pumpkin.getDefaultState());
    }
    private void renderEastWestSegment(BlockPos p0, BlockPos p1,
                                       EnumFacing direction, double segment_length, boolean last_segment,
                                       World world, IChunkProvider chunkProvider) {
        double deltax = p1.getX() - p0.getX();
        double deltaz = p1.getZ() - p0.getZ();
        int zInc = deltaz < 0 ? -1 : 1;
        int xInc = deltax < 0 ? -1 : 1;
        double error = 0;
        double deltaerr = Math.abs(deltaz / deltax);
        int z = p0.getZ();
        int x0 = p0.getX();
        int x1 = p1.getX();
        for (int x = x0; compare(x, x1, xInc); x += xInc) {
            BlockPos pos = calculate_height(new BlockPos(x, 64, z), p0, p1, segment_length);
            RoadBlock block = addRoadBlock(pos, direction, world, chunkProvider);

            error = error + deltaerr;
            while (error >= 0.5) {
                z = z + zInc;
                error = error - 1.0;

                if (zInc > 0) {
                    if (direction == EnumFacing.EAST)
                        block.right++;
                    else
                        block.left++;
                } else {
                    if (direction == EnumFacing.EAST)
                        block.left++;
                    else
                        block.right++;
                }
            }
        }
        if (last_segment) {
            BlockPos pos = calculate_height(p1, p0, p1, segment_length);
            addRoadBlock(pos, direction, world, chunkProvider);
        }
        world.setBlockState(p0.up(4), Blocks.lit_pumpkin.getDefaultState());
    }
    private void renderSegment(BlockPos p0, BlockPos p1, boolean lastSegment,
                               World world, IChunkProvider chunkProvider) {
        double deltax = p1.getX() - p0.getX();
        double deltaz = p1.getZ() - p0.getZ();
        int zInc = deltaz < 0 ? -1 : 1;
        int xInc = deltax < 0 ? -1 : 1;
        int z0 = p0.getZ();
        int z1 = p1.getZ();
        int x0 = p0.getX();
        int x1 = p1.getX();

        double dx = p1.getX() - p0.getX();
        double dz = p1.getZ() - p0.getZ();
        double dy = Math.abs(p1.getY() - p0.getY());

        EnumFacing direction = EnumFacing.getFacingFromVector((float) dx, 0, (float) dz);
        double segment_length = Math.sqrt(dx * dx + dz * dz);


        if (deltax == 0) { // verticle line
            for (int z = z0; compare(z, z1, zInc); z += zInc) {
                BlockPos pos = calculate_height(new BlockPos(x0, 64, z), p0, p1, segment_length);
                addRoadBlock(pos, zInc < 0 ? EnumFacing.NORTH : EnumFacing.SOUTH, world, chunkProvider);
            }
        } else if (deltaz == 0) {
            for (int x = x0; compare(x, x1, xInc); x += xInc) {
                BlockPos pos = calculate_height(new BlockPos(x, 64, z0), p0, p1, segment_length);
                addRoadBlock(pos, xInc < 0 ? EnumFacing.WEST : EnumFacing.EAST, world, chunkProvider);
            }
        } else {
            if (direction == EnumFacing.EAST || direction == EnumFacing.WEST) {
                renderEastWestSegment(p0, p1, direction, segment_length, lastSegment, world, chunkProvider);
            } else {
                renderNorthSouthSegment(p0, p1, direction, segment_length, lastSegment, world, chunkProvider);
            }
        }
    }
    public static List bad_biomes = Arrays.asList(new BiomeGenBase[]{
            BiomeGenBase.deepOcean, BiomeGenBase.ocean, BiomeGenBase.frozenOcean
    });

    // Build a road from structure0 to structure1
    // returns true if road built successfully
    public boolean buildRoad(Structure s0, Structure s1, Random random, World world, IChunkProvider chunkProvider) {
        point_list = new ArrayList<BlockPos>();
        Stack<BlockPos> stack = new Stack<BlockPos>();
        structure0 = s0;
        structure1 = s1;

        Structure.RoadPoint pt0 = s0.selectRoadPoint(s1);
        if (pt0 == null) return false;
        Structure.RoadPoint pt1 = s1.selectRoadPoint(s0);
        if (pt1 == null) return false;

        stack.push(pt1.getPosition());
        BlockPos p0 = pt0.getPosition();
        BiomeGenBase biome = world.getBiomeGenForCoords(p0);
        if (bad_biomes.contains(biome)) return false;

        double dx = pt1.getPosition().getX() - p0.getX();
        double dz = pt1.getPosition().getZ() - p0.getZ();
        double dy = Math.abs(pt1.getPosition().getY() - p0.getY());

        EnumFacing direction = EnumFacing.getFacingFromVector((float)dx, 0, (float)dz);
        double distance = Math.abs((direction == EnumFacing.EAST || direction == EnumFacing.WEST) ? dx : dz);
        //double distance = Math.sqrt(dx*dx + dz*dz);

        // If the change in height is greater than the distance, we cannot build a road to it
        if (dy >= distance) {
            //System.out.println(String.format("  -- Rejecting Road: TOO STEEP: dy: %5.1f, length: %5.1f", dy, distance));
            return false;
        }

        final int MAX_FAILS = 5;
        int failed_tries = 0;
        while(stack.size() > 0 && failed_tries < MAX_FAILS) {
            BlockPos p1 = stack.pop();
            dx = p1.getX() - p0.getX();
            dz = p1.getZ() - p0.getZ();
            dy = Math.abs(p1.getY() - p0.getY());

            //distance = Math.sqrt(dx*dx + dz*dz);
            distance = Math.abs((direction == EnumFacing.EAST || direction == EnumFacing.WEST) ? dx : dz);
            if (dy > Math.floor( 4.0 * distance / 5.0)) {
                failed_tries++;
                //System.out.println(String.format("  -- Rejecting segment: TOO STEEP: dy: %5.1f, length: %5.1f, %d tries", dy, distance, failed_tries));
                if (stack.isEmpty()) return false;
                continue;
            }
            if (distance > MAX_SEGMENT_LENGTH) {
                BlockPos center = getCenter(p0, p1);
                double jitter = (2*distance/5.0); // changed from 3.0
                double jitterX = jitter * random.nextDouble() - jitter/2.0;
                double jitterZ = jitter * random.nextDouble() - jitter/2.0;

                center = center.east((int)Math.floor(jitterX)).south((int) Math.floor(jitterZ));
                int h = TerrainMap.getGroundOrWaterLevel(center, world, chunkProvider);
                center = new BlockPos(center.getX(), h, center.getZ());
                while (world.isAirBlock(center))
                    center = center.down();

                dy = Math.abs(p1.getY() - center.getY());
                distance = Math.abs((direction == EnumFacing.EAST || direction == EnumFacing.WEST) ? center.getX() - p0.getX() : center.getZ() - p0.getZ());
                if (dy > Math.floor( 4.0 * distance / 5.0)) {
                    failed_tries++;
                    //System.out.println(String.format("  -- Rejecting center: TOO STEEP: dy: %5.1f, length: %5.1f, %d tries", dy, distance, failed_tries));
                    if (stack.isEmpty()) return false;
                    continue;
                }

                stack.push(p1);
                stack.push(center);
            } else {
                point_list.add(p0);
                p0 = p1;
                biome = world.getBiomeGenForCoords(p0);
                if (bad_biomes.contains(biome)) return false;
            }
        }
        if (failed_tries >= MAX_FAILS) return false;

        biome = world.getBiomeGenForCoords(p0);
        if (bad_biomes.contains(biome)) return false;
        point_list.add(p0);

        // At this point our point list is filled in, do we need to build segment list
        pt0.setUsed();
        pt1.setUsed();
        //System.out.println(String.format("Rendering Road from %s to %s", structure0.position.toString(), structure1.position.toString()));

        if (point_list.size() > 2) {
            // Smooth out anamolous points
            if (point_list.size() > 3) {
                // TODO - This misses an endpoint (I think)
                for(int i = 3; i < point_list.size(); i++) {
                    int y0 = point_list.get(i - 2).getY();
                    int y1 = point_list.get(i - 1).getY();
                    int y2 = point_list.get(i).getY();

                    int d0 = Math.abs(y1 - y0);
                    int d1 = Math.abs(y1 - y2);

                    if (((y0 < y1 && y2 < y1) || (y0 > y1 && y2 > y1)) && d0 >= 5 && d1 >= 5) {
                        int x = (point_list.get(i-2).getX() + point_list.get(i).getX()) / 2;
                        int y = (y0 + y2) / 2;
                        int z = (point_list.get(i-2).getZ() + point_list.get(i).getZ()) / 2;

                        point_list.set(i - 1, new BlockPos(x, y, z));
                    }
                }
            }
            p0 = point_list.get(0);
            for (int i = 1; i < point_list.size(); i++) {
                BlockPos p1 = point_list.get(i);
                renderSegment(p0, p1, (i == (point_list.size() - 1)), world, chunkProvider);
                p0 = p1;
            }
            // Now the RoadBlock list is populated. Go through it and add bridges, etc.

            for (int i = 0; i < blockList.size(); i++) {
                RoadBlock roadBlock = blockList.get(i);
                IBlockState blockState = Blocks.stonebrick.getDefaultState();
                IBlockState blockState1 = Blocks.brick_block.getDefaultState();
                if (i + 1 < blockList.size()) {
                    RoadBlock nextBlock = blockList.get(i+1);
                    int nextY = nextBlock.pos.getY();
                    boolean isStairs = false;
                    boolean nextIsStairs = false;
                    if (nextY < roadBlock.pos.getY()) {
                        blockState = Blocks.stone_brick_stairs.getDefaultState().withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM).withProperty(BlockStairs.SHAPE, BlockStairs.EnumShape.STRAIGHT).withProperty(BlockStairs.FACING, roadBlock.direction.getOpposite());
                        blockState1 = Blocks.brick_stairs.getDefaultState().withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM).withProperty(BlockStairs.SHAPE, BlockStairs.EnumShape.STRAIGHT).withProperty(BlockStairs.FACING, roadBlock.direction.getOpposite());
                        isStairs = true;
                    } else if (nextY > roadBlock.pos.getY()) {
                        blockState = Blocks.stone_brick_stairs.getDefaultState().withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM).withProperty(BlockStairs.SHAPE, BlockStairs.EnumShape.STRAIGHT).withProperty(BlockStairs.FACING, roadBlock.direction);
                        blockState1 = Blocks.brick_stairs.getDefaultState().withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM).withProperty(BlockStairs.SHAPE, BlockStairs.EnumShape.STRAIGHT).withProperty(BlockStairs.FACING, roadBlock.direction);
                        roadBlock.pos = roadBlock.pos.up();
                        isStairs = true;
                    }
                    if (i + 2 < blockList.size() && blockList.get(i+2).pos.getY() != nextY) {
                        nextIsStairs = true;
                    }

                    if (isStairs || nextIsStairs) {
                        // I'm a stair, make sure the next chunk of road is wide enough for me. (stairs to nowhere are ugly)
                        int leftOffset = 0;
                        if (roadBlock.direction == EnumFacing.EAST)
                            leftOffset = nextBlock.pos.getZ() - roadBlock.pos.getZ();
                        if (roadBlock.direction == EnumFacing.WEST)
                            leftOffset = roadBlock.pos.getZ() - nextBlock.pos.getZ();
                        if (roadBlock.direction == EnumFacing.NORTH)
                            leftOffset = nextBlock.pos.getX() - roadBlock.pos.getX();
                        if (roadBlock.direction == EnumFacing.SOUTH)
                            leftOffset = roadBlock.pos.getX() - nextBlock.pos.getX();
                        // now see if 'next' is stairs, and make sure we catch all of him
                        if (nextIsStairs) {
                            roadBlock.left = Math.max(roadBlock.left, nextBlock.left - leftOffset);
                            roadBlock.right = Math.max(roadBlock.right, nextBlock.right + leftOffset);
                        }

                        if (isStairs) {
                            nextBlock.left = Math.max(nextBlock.left, roadBlock.left + leftOffset);
                            nextBlock.right = Math.max(nextBlock.right, roadBlock.right - leftOffset);
                        }
                    }
                }
                updateVillageBoxes(world);
                plot(roadBlock.pos, blockState1, world, chunkProvider);
                int sign_rot = 0;
                if (roadBlock.direction == EnumFacing.EAST) {
                    sign_rot = 4;
                    for (int l = 0; l < roadBlock.left; l++)
                        plot(roadBlock.pos.north(l + 1), blockState, world, chunkProvider);
                    for (int r = 0; r < roadBlock.right; r++)
                        plot(roadBlock.pos.south(r + 1), blockState, world, chunkProvider);
                }
                if (roadBlock.direction == EnumFacing.WEST) {
                    sign_rot = 12;
                    for (int l = 0; l < roadBlock.left; l++)
                        plot(roadBlock.pos.south(l + 1), blockState, world, chunkProvider);
                    for (int r = 0; r < roadBlock.right; r++)
                        plot(roadBlock.pos.north(r + 1), blockState, world, chunkProvider);
                }
                if (roadBlock.direction == EnumFacing.SOUTH) {
                    sign_rot = 8;
                    for (int l = 0; l < roadBlock.left; l++)
                        plot(roadBlock.pos.east(l + 1), blockState, world, chunkProvider);
                    for (int r = 0; r < roadBlock.right; r++)
                        plot(roadBlock.pos.west(r + 1), blockState, world, chunkProvider);
                }
                if (roadBlock.direction == EnumFacing.NORTH) {
                    sign_rot = 0;
                    for (int l = 0; l < roadBlock.left; l++)
                        plot(roadBlock.pos.west(l + 1), blockState, world, chunkProvider);
                    for (int r = 0; r < roadBlock.right; r++)
                        plot(roadBlock.pos.east(r + 1), blockState, world, chunkProvider);
                }
                if (i == 0) {
                    IBlockState signBlockState = Blocks.standing_sign.getDefaultState().withProperty(BlockStandingSign.ROTATION, sign_rot);
                    BlockStandingSign signBlock = (BlockStandingSign)signBlockState.getBlock();
                    TileEntity tileentity = signBlock.createNewTileEntity(world, signBlock.getMetaFromState(signBlockState));
                    if (tileentity instanceof TileEntitySign) {
                        TileEntitySign entity = (TileEntitySign)tileentity;
                        entity.signText[0] = new ChatComponentText("START");
                        entity.signText[1] = new ChatComponentText(roadBlock.direction.getName().toUpperCase());
                        entity.signText[2] = new ChatComponentText("ROAD");
                        entity.signText[3] = new ChatComponentText(String.format("(%d m)", blockList.size()));
                        entity.markDirty();
                        world.setBlockState(roadBlock.pos.up(), signBlock.getActualState(signBlockState, world, roadBlock.pos.up()));
                        world.setTileEntity(roadBlock.pos.up(), entity);
                    }
                } else if (i == blockList.size() - 1) {
                    IBlockState signBlockState = Blocks.standing_sign.getDefaultState().withProperty(BlockStandingSign.ROTATION, sign_rot);
                    BlockStandingSign signBlock = (BlockStandingSign)signBlockState.getBlock();
                    TileEntity tileentity = signBlock.createNewTileEntity(world, signBlock.getMetaFromState(signBlockState));
                    if (tileentity instanceof TileEntitySign) {
                        TileEntitySign entity = (TileEntitySign)tileentity;
                        entity.signText[0] = new ChatComponentText("END");
                        entity.signText[1] = new ChatComponentText(roadBlock.direction.getName().toUpperCase());
                        entity.signText[2] = new ChatComponentText("ROAD");
                        entity.signText[3] = new ChatComponentText(String.format("(%d m)", blockList.size()));
                        entity.markDirty();
                        world.setBlockState(roadBlock.pos.up(), signBlock.getActualState(signBlockState, world, roadBlock.pos.up()));
                        world.setTileEntity(roadBlock.pos.up(), entity);
                    }
                }
            }
        }
        return true;
    }
}
