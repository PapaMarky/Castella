package com.markyshouse.mc.Castella;

import com.markyshouse.mc.TerrainMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import sun.net.www.http.ChunkedInputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

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

        return new BlockPos(b.getX(), Math.round(h), b.getZ());
    }
    private void renderSegment(BlockPos p0, BlockPos p1, World world, IChunkProvider chunkProvider) {
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

        EnumFacing direction = EnumFacing.getFacingFromVector((float)dx, 0, (float)dz);

        if (dy > segment_length) {
            System.out.println("TOO MUCH RISE IN ROAD");
        }

        if (deltax == 0) { // verticle line
            for (int z = z0; compare(z, z1, zInc); z += zInc) {
                BlockPos pos = new BlockPos(x0, 64, z);
                plot(calculate_height(pos.east(), p0, p1, segment_length), Blocks.stonebrick.getDefaultState(), world, chunkProvider);
                plot(calculate_height(pos, p0, p1, segment_length), Blocks.stonebrick.getDefaultState(), world, chunkProvider);
                plot(calculate_height(pos.west(), p0, p1, segment_length), Blocks.stonebrick.getDefaultState(), world, chunkProvider);
                //plot(calculate_height(new BlockPos(x0, 64, z), p0, p1, segment_length), Blocks.brick_block.getDefaultState(), world, chunkProvider);
            }
        } else if (deltaz == 0) {
            for (int x = x0; compare(x, x1, xInc); x += xInc) {
                BlockPos pos = new BlockPos(x, 64, z0);
                plot(calculate_height(pos.north(), p0, p1, segment_length), Blocks.stonebrick.getDefaultState(), world, chunkProvider);
                plot(calculate_height(pos, p0, p1, segment_length), Blocks.stonebrick.getDefaultState(), world, chunkProvider);
                plot(calculate_height(pos.south(), p0, p1, segment_length), Blocks.stonebrick.getDefaultState(), world, chunkProvider);
                //calculate_height(plot(new BlockPos(x, 64, z0), p0, p1, segment_length), Blocks.coal_block.getDefaultState(), world, chunkProvider);
            }
        } else {
            double error = 0;
            double deltaerr = Math.abs(deltaz / deltax);
            int z = p0.getZ();
            for (int x = x0; compare(x, x1, xInc); x += xInc) {
                BlockPos blockPos = new BlockPos(x, 64, z);
                BlockPos pos = new BlockPos(x, 64, z);
                plot(calculate_height(pos, p0, p1, segment_length), Blocks.stonebrick.getDefaultState(), world, chunkProvider);
                if (direction == EnumFacing.EAST || direction == EnumFacing.WEST) {
                    plot(calculate_height(pos.north(), p0, p1, segment_length), Blocks.stonebrick.getDefaultState(), world, chunkProvider);
                    plot(calculate_height(pos.south(), p0, p1, segment_length), Blocks.stonebrick.getDefaultState(), world, chunkProvider);
                } else {
                    plot(calculate_height(pos.east(), p0, p1, segment_length), Blocks.stonebrick.getDefaultState(), world, chunkProvider);
                    plot(calculate_height(pos.west(), p0, p1, segment_length), Blocks.stonebrick.getDefaultState(), world, chunkProvider);
                }
               //  plot(calculate_height(new BlockPos(x, 64, z), p0, p1, segment_length), Blocks.planks.getDefaultState(), world, chunkProvider);

                error = error + deltaerr;
                while (error >= 0.5) {
                    z = z + zInc;
                    error = error - 1.0;
                    pos = new BlockPos(x, 64, z);
                    plot(calculate_height(pos, p0, p1, segment_length), Blocks.stonebrick.getDefaultState(), world, chunkProvider);
                    if (direction == EnumFacing.EAST || direction == EnumFacing.WEST) {
                        plot(calculate_height(pos.north(), p0, p1, segment_length), Blocks.stonebrick.getDefaultState(), world, chunkProvider);
                        plot(calculate_height(pos.south(), p0, p1, segment_length), Blocks.stonebrick.getDefaultState(), world, chunkProvider);
                    } else {
                        plot(calculate_height(pos.east(), p0, p1, segment_length), Blocks.stonebrick.getDefaultState(), world, chunkProvider);
                        plot(calculate_height(pos.west(), p0, p1, segment_length), Blocks.stonebrick.getDefaultState(), world, chunkProvider);
                    }
                    //plot(calculate_height(new BlockPos(x, 64, z), p0, p1, segment_length), Blocks.red_mushroom_block.getDefaultState(), world, chunkProvider);
                }
            }
        }
        world.setBlockState(p0.up(3), Blocks.lit_pumpkin.getDefaultState());
    }
    // Build a road from structure0 to structure1
    // returns true if road built successfully
    public boolean buildRoad(Structure s0, Structure s1, Random random, World world, IChunkProvider chunkProvider) {
        point_list = new ArrayList<BlockPos>();
        Stack<BlockPos> stack = new Stack<BlockPos>();
        structure0 = s0;
        structure1 = s1;

        // TODO validate that structures have road heads that are not in use
        // TODO choose which road head to use for each structure

        Structure.RoadPoint pt0 = s0.selectRoadPoint(s1);
        if (pt0 == null) return false;
        Structure.RoadPoint pt1 = s1.selectRoadPoint(s0);
        if (pt1 == null) return false;

        stack.push(pt1.getPosition());
        BlockPos p0 = pt0.getPosition();

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
            if (dy > distance) {
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

                stack.push(p1);
                stack.push(center);
            } else {
                point_list.add(p0);
                p0 = p1;
            }
        }
        if (failed_tries >= MAX_FAILS) return false;
        point_list.add(p0);

        // At this point our point list is filled in, do we need to build segment list
        pt0.setUsed();
        pt1.setUsed();
        System.out.println(String.format("Rendering Road from %s to %s", structure0.position.toString(), structure1.position.toString()));

        if (point_list.size() > 2) {
            p0 = point_list.get(0);
            /*
            for (int ii = 3; ii < 10; ii++)
                world.setBlockState(p0.up(ii), Blocks.brick_block.getDefaultState());
                */
            for (int i = 1; i < point_list.size(); i++) {
                BlockPos p1 = point_list.get(i);
                System.out.println(String.format(" -- segment %s to %s", p0, p1));
                renderSegment(p0, p1, world, chunkProvider);
                p0 = p1;
                /*
                for (int j = 3; j < 10; j++)
                    world.setBlockState(p0.up(j), Blocks.brick_block.getDefaultState());
                    */
            }
        }
        for (int j = 1; j < 10; j++) {
            world.setBlockState(pt0.getPosition().up(j), Blocks.redstone_block.getDefaultState());
            world.setBlockState(pt1.getPosition().up(j), Blocks.lapis_block.getDefaultState());
        }
        return true;
    }
}
