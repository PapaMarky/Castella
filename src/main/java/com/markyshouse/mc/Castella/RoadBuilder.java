package com.markyshouse.mc.Castella;

import com.markyshouse.mc.TerrainMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
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
    private double MAX_SEGMENT_LENGTH = 50;

    public BlockPos getCenter(BlockPos p0, BlockPos p1) {
        int x = p0.getX() + (p1.getX() - p0.getX())/2;
        int y = p0.getY() + (p1.getY() - p0.getY())/2;
        int z = p0.getZ() + (p1.getZ() - p0.getZ())/2;
        return new BlockPos(x, y, z);
    }

    public RoadBuilder() {
    }

    private void plot(BlockPos blockPos, IBlockState blockState, World world, IChunkProvider chunkProvider) {
        int h = TerrainMap.getGroundOrWaterLevel(blockPos, world, chunkProvider);
        blockPos = new BlockPos(blockPos.getX(), h, blockPos.getZ());

        Block block2 = chunkProvider.provideChunk(blockPos.up()).getBlock(blockPos.up());
        while (TerrainMap.isLiquid(block2)) {
            blockPos = blockPos.up();
            block2 = chunkProvider.provideChunk(blockPos.up()).getBlock(blockPos.up());
        }
        System.out.println(String.format("   -- plot %s", block2.toString()));
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
    private void renderSegment(BlockPos p0, BlockPos p1, World world, IChunkProvider chunkProvider) {
        double deltax = p1.getX() - p0.getX();
        double deltaz = p1.getZ() - p0.getZ();
        int zInc = deltaz < 0 ? -1 : 1;
        int xInc = deltax < 0 ? -1 : 1;
        int z0 = p0.getZ();
        int z1 = p1.getZ();
        int x0 = p0.getX();
        int x1 = p1.getX();

        if (deltax == 0) { // verticle line
            for (int z = z0; z < z1; z++) {
                plot(new BlockPos(x0, 64, z), Blocks.brick_block.getDefaultState(), world, chunkProvider);
            }
        } else {
            double error = 0;
            double deltaerr = Math.abs(deltaz / deltax);
            int z = p0.getZ();
            for (int x = x0; compare(x, x1, xInc); x += xInc) {
                BlockPos blockPos = new BlockPos(x, 64, z);
                plot(new BlockPos(x, 64, z), Blocks.stonebrick.getDefaultState(), world, chunkProvider);

                error = error + deltaerr;
                while (error >= 0.5) {
                    z = z + zInc;
                    error = error - 1.0;

                    plot(new BlockPos(x, 64, z), Blocks.stonebrick.getDefaultState(), world, chunkProvider);
                }
            }
        }
    }
    // Build a road from structure0 to structure1
    public void buildRoad(Structure s0, Structure s1, Random random, World world, IChunkProvider chunkProvider) {
        point_list = new ArrayList<BlockPos>();
        Stack<BlockPos> stack = new Stack<BlockPos>();
        structure0 = s0;
        structure1 = s1;

        // TODO validate that structures have road heads that are not in use
        // TODO choose which road head to use for each structure

        stack.push(s1.position);
        BlockPos p0 = s0.position;

        while(stack.size() > 0) {
            BlockPos p1 = stack.pop();
            double dx = p1.getX() - p0.getX();
            double dz = p1.getZ() - p0.getZ();

            double distance = Math.sqrt(dx*dx + dz*dz);
            if (distance > MAX_SEGMENT_LENGTH) {
                BlockPos center = getCenter(p0, p1);
                double jitter = (distance/3.0);
                double jitterX = jitter * random.nextDouble() - jitter/2.0;
                double jitterZ = jitter * random.nextDouble() - jitter/2.0;

                center = center.east((int)Math.floor(jitterX)).south((int) Math.floor(jitterZ));
                int h = TerrainMap.getGroundOrWaterLevel(center, world, chunkProvider);
                center = new BlockPos(center.getX(), h, center.getZ());
                /*
                Chunk chunk = chunkProvider.provideChunk(center);
                Block block0 = chunk.getBlock(center.down());
                Block block = chunk.getBlock(center);
                Block block1 = chunk.getBlock(center.up());
                */

                stack.push(p1);
                stack.push(center);
            } else {
                point_list.add(p0);
                p0 = p1;
            }
        }
        point_list.add(p0);
        // At this point our point list is filled in, do we need to build segment list
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
    }
}
