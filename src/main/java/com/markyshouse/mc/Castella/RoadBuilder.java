package com.markyshouse.mc.Castella;

import com.markyshouse.mc.TerrainMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import sun.net.www.http.ChunkedInputStream;

import java.util.*;

/**
 * Created by mark on 10/2/2015.
 */
public class RoadBuilder {

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

    public BlockPos getCenter(BlockPos p0, BlockPos p1) {
        int x = p0.getX() + (p1.getX() - p0.getX())/2;
        int y = p0.getY() + (p1.getY() - p0.getY())/2;
        int z = p0.getZ() + (p1.getZ() - p0.getZ())/2;
        return new BlockPos(x, y, z);
    }

    public RoadBuilder() {
    }

    private void plot(BlockPos blockPos, IBlockState blockState, World world, IChunkProvider chunkProvider) {
        Chunk chunk = chunkProvider.provideChunk(blockPos);
        int h = chunk.getHeight(blockPos);
        int n = 0;
        final int TUNNEL_HEIGHT = 3;
        BlockPos pos = blockPos.up();
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
            world.destroyBlock(pos, false);
            world.setBlockState(pos, Blocks.brick_block.getDefaultState());
            hh--;
        }
        while (hh > blockPos.getY()) {
            pos = new BlockPos(blockPos.getX(), hh, blockPos.getZ());
            world.destroyBlock(pos, false);
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

        return new BlockPos(b.getX(), Math.floor(h), b.getZ());
    }

    private void renderNorthSouthSegment(BlockPos p0, BlockPos p1,
                                         EnumFacing direction, double segment_length, boolean last_segment,
                                         IBlockState blockState, World world, IChunkProvider chunkProvider) {
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
            plot(pos, Blocks.stonebrick.getDefaultState(), world, chunkProvider);
            plot(pos.east(), blockState, world, chunkProvider);
            plot(pos.west(), blockState, world, chunkProvider);
            //  plot(calculate_height(new BlockPos(x, 64, z), p0, p1, segment_length), Blocks.planks.getDefaultState(), world, chunkProvider);

            error = error + deltaerr;
            while (error >= 0.5) {
                x = x + xInc;
                error = error - 1.0;
                //pos = calculate_height(new BlockPos(x, 64, z), p0, p1, segment_length);
                // plot(pos, blockState, world, chunkProvider);
                if (xInc > 0)
                    plot(pos.east(), blockState, world, chunkProvider);
                else
                    plot(pos.west(), blockState, world, chunkProvider);
                //plot(calculate_height(new BlockPos(x, 64, z), p0, p1, segment_length), Blocks.red_mushroom_block.getDefaultState(), world, chunkProvider);
            }
        }

        /*
        plot(p0.east(), Blocks.gold_block.getDefaultState(), world, chunkProvider);
        plot(p0, Blocks.gold_block.getDefaultState(), world, chunkProvider);
        plot(p0.west(), Blocks.gold_block.getDefaultState(), world, chunkProvider);
        */
        if (last_segment) {
            BlockPos pos = calculate_height(p1, p0, p1, segment_length);
            plot(pos.east(), Blocks.lapis_block.getDefaultState(), world, chunkProvider);
            plot(pos, Blocks.lapis_block.getDefaultState(), world, chunkProvider);
            plot(pos.west(), Blocks.lapis_block.getDefaultState(), world, chunkProvider);
        }
        world.setBlockState(p0.up(4), Blocks.lit_pumpkin.getDefaultState());
    }
    private void renderEastWestSegment(BlockPos p0, BlockPos p1,
                                       EnumFacing direction, double segment_length, boolean last_segment,
                                       IBlockState blockState, World world, IChunkProvider chunkProvider) {
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
            plot(pos, Blocks.stonebrick.getDefaultState(), world, chunkProvider);
            plot(pos.north(), blockState, world, chunkProvider);
            plot(pos.south(), blockState, world, chunkProvider);
            //  plot(calculate_height(new BlockPos(x, 64, z), p0, p1, segment_length), Blocks.planks.getDefaultState(), world, chunkProvider);

            error = error + deltaerr;
            while (error >= 0.5) {
                z = z + zInc;
                error = error - 1.0;
                //pos = calculate_height(new BlockPos(x, 64, z), p0, p1, segment_length);
                // plot(pos, blockState, world, chunkProvider);

                if (zInc > 0)
                    plot(pos.south(), blockState, world, chunkProvider);
                else
                    plot(pos.north(), blockState, world, chunkProvider);
                //plot(calculate_height(new BlockPos(x, 64, z), p0, p1, segment_length), Blocks.red_mushroom_block.getDefaultState(), world, chunkProvider);
            }
        }

        /*
        BlockPos pos = calculate_height(p0, p0, p1, segment_length);
        plot(pos.north(), Blocks.gold_block.getDefaultState(), world, chunkProvider);
        plot(pos, Blocks.gold_block.getDefaultState(), world, chunkProvider);
        plot(pos.south(), Blocks.gold_block.getDefaultState(), world, chunkProvider);
        */
        if (last_segment) {
            BlockPos pos = calculate_height(p1, p0, p1, segment_length);
            plot(pos.north(), Blocks.gold_block.getDefaultState(), world, chunkProvider);
            plot(pos, Blocks.gold_block.getDefaultState(), world, chunkProvider);
            plot(pos.south(), Blocks.gold_block.getDefaultState(), world, chunkProvider);
        }
        world.setBlockState(p0.up(4), Blocks.lit_pumpkin.getDefaultState());
    }
    private void renderSegment(BlockPos p0, BlockPos p1, IBlockState blockState, boolean lastSegment,
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

        double segment_length = Math.sqrt(dx*dx + dz*dz);

        if (dy > segment_length) {
            System.out.println("TOO MUCH RISE IN ROAD");
        }

        EnumFacing direction = EnumFacing.getFacingFromVector((float)dx, 0, (float)dz);

        if (deltax == 0) { // verticle line
            for (int z = z0; compare(z, z1, zInc); z += zInc) {
                BlockPos pos = calculate_height(new BlockPos(x0, 64, z), p0, p1, segment_length);
                plot(pos.east(), blockState, world, chunkProvider);
                plot(pos, blockState, world, chunkProvider);
                plot(pos.west(), blockState, world, chunkProvider);
                //plot(calculate_height(new BlockPos(x0, 64, z), p0, p1, segment_length), Blocks.brick_block.getDefaultState(), world, chunkProvider);
            }
        } else if (deltaz == 0) {
            for (int x = x0; compare(x, x1, xInc); x += xInc) {
                BlockPos pos = calculate_height(new BlockPos(x, 64, z0), p0, p1, segment_length);
                plot(pos.north(), blockState, world, chunkProvider);
                plot(pos, blockState, world, chunkProvider);
                plot(pos.south(), blockState, world, chunkProvider);
                //calculate_height(plot(new BlockPos(x, 64, z0), p0, p1, segment_length), Blocks.coal_block.getDefaultState(), world, chunkProvider);
            }
        } else {
            if (direction == EnumFacing.EAST || direction == EnumFacing.WEST) {
                renderEastWestSegment(p0, p1, direction, segment_length, lastSegment, blockState, world, chunkProvider);
            } else {
                renderNorthSouthSegment(p0, p1, direction, segment_length, lastSegment, blockState, world, chunkProvider);
            }
        }
    }
    // Build a road from structure0 to structure1
    // returns true if road built successfully
    public static List bad_biomes = Arrays.asList(new BiomeGenBase[]{
            BiomeGenBase.deepOcean, BiomeGenBase.ocean, BiomeGenBase.frozenOcean
    });

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

        double distance = Math.sqrt(dx*dx + dz*dz);

        // If the change in height is greater than the distance, we cannot build a road to it
        if (dy > distance) return false;

        final int MAX_FAILS = 5;
        int failed_tries = 0;
        while(stack.size() > 0 && failed_tries < MAX_FAILS) {
            BlockPos p1 = stack.pop();
            dx = p1.getX() - p0.getX();
            dz = p1.getZ() - p0.getZ();
            dy = Math.abs(p1.getY() - p0.getY());

            distance = Math.sqrt(dx*dx + dz*dz);
            if (dy > Math.floor( 4.0 * distance / 5.0)) {
                failed_tries++;
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
                if (dy > Math.floor( 4.0 * distance / 5.0)) {
                    failed_tries++;
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
            // Smooth out anomolous points
            if (point_list.size() > 3) {
                // TODO - This misses an endpoint (I think)
                for(int i = 3; i < point_list.size(); i++) {
                    int y0 = point_list.get(i - 2).getY();
                    int y1 = point_list.get(i - 1).getY();
                    int y2 = point_list.get(i).getY();

                    int d0 = Math.abs(y1 - y0);
                    int d1 = Math.abs(y1 - y2);
                    int dmin = Math.min(Math.abs(y1 - y0), Math.abs(y2 - y1));

                    if (((y0 < y1 && y2 < y1) || (y0 > y1 && y2 > y1)) && d0 >= 5 && d1 >= 5) {
                        int x = (point_list.get(i-2).getX() + point_list.get(i).getX()) / 2;
                        int y = (y0 + y2) / 2;
                        int z = (point_list.get(i-2).getZ() + point_list.get(i).getZ()) / 2;

                        point_list.set(i - 1, new BlockPos(x, y, z));
                    }
                }
            }
            p0 = point_list.get(0);
            /*
            for (int ii = 3; ii < 10; ii++)
                world.setBlockState(p0.up(ii), Blocks.brick_block.getDefaultState());
                */
            for (int i = 1; i < point_list.size(); i++) {
                BlockPos p1 = point_list.get(i);
                //System.out.println(String.format(" -- segment %s to %s", p0, p1));
                renderSegment(p0, p1, Blocks.stonebrick.getDefaultState(), (i == (point_list.size() - 1)), world, chunkProvider);
                p0 = p1;
                /*
                for (int j = 3; j < 10; j++)
                    world.setBlockState(p0.up(j), Blocks.brick_block.getDefaultState());
                    */
            }
        }
        /*
        for (int j = 5; j < 10; j++) {
            world.setBlockState(pt0.getPosition().up(j), Blocks.redstone_block.getDefaultState());
            world.setBlockState(pt1.getPosition().up(j), Blocks.lapis_block.getDefaultState());
        }
        */
        return true;
    }
}
