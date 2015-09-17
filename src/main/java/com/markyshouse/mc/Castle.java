package com.markyshouse.mc;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.block.Block;

import java.util.Map;

/**
 * Created by mark on 9/15/2015.
 */
public class Castle {
    static public int TOWER_SIZE = 9;
    static public int GROUNDS_SIZE = 15;
    static public int SLOPE_SIZE = 25;
    private BlockPos _pos;
    private World _world;
    private IChunkProvider _chunk;
    private static boolean isLevelEnough(int x, int z, HeightAnalyser analyser) {
        //analyser.analyze(new BlockPos(x, 64.0, z), SLOPE_SIZE, SLOPE_SIZE);
        analyser.analyze(new BlockPos(x, 64.0, z), GROUNDS_SIZE, GROUNDS_SIZE);
        return analyser.max_height - analyser.min_height < TOWER_SIZE;
    }
    public static BlockPos canBuildHere(int x, int z, World world, IChunkProvider chunkProvider) {
        HeightAnalyser analyser = new HeightAnalyser(world, chunkProvider);
        BlockPos try_pos = new BlockPos(x, 64, z);
        Chunk chunk = chunkProvider.provideChunk(try_pos);
        BiomeGenBase biome = chunk.getBiome(try_pos, world.getWorldChunkManager());

        // skip places that are not castle friendly
        switch (biome.biomeID) {
            case 0: // ocean
            //case 3: // extreme hills
            case 7: // river
            case 8: // hell
            case 9: // sky
            case 10: // frozen ocean
            case 11: // frozen river
            case 20: // extreme hills edge
            case 24: // deep ocean
            case 29: // roofed forest
                return null;
        }
        int JITTER = TOWER_SIZE/2;
        boolean found = false;
        for (int jitter_x = -1; !found && jitter_x <= 1; jitter_x++) {
            for (int jitter_z = -1; !found && jitter_z <= 1; jitter_z++) {
                int jx = x + (jitter_x * JITTER);
                int jz = z + (jitter_z * JITTER);
                if (isLevelEnough(jx, jz, analyser)) {
                    System.out.println("** location jitter: " + jx + ", " + jz + " -- " + analyser.average_height + " " + biome.biomeName);
                    return new BlockPos(x, analyser.average_height, z);
                }
            }
        }
        return null;
    }
    public BlockPos get_position() { return _pos;}
    public Castle(BlockPos pos) {
        _pos = pos;
    }

    private int dist(int x0, int z0, int x1, int z1) {
        int dx = x0 - x1;
        int dz = z0 - z1;
        return (int)Math.round(Math.sqrt(dx * dx + dz * dz));
    }
    // TODO bulldozer class
    void setTerrainHeight(int x, int target_h, int z, World world, IChunkProvider chunkProvider, IBlockState digBlock, IBlockState fillBlock, IBlockState sameBlock) {
        BlockPos pos = new BlockPos(x, target_h, z);
        Chunk chunk = chunkProvider.provideChunk(pos);
        int h = chunk.getHeight(pos); // HeightAnalyser.get_ground_level(pos, chunkProvider);

        if (h > target_h) {
            // need to dig
            IBlockState bs = digBlock; // Blocks.emerald_block.getDefaultState();
            IBlockState glass = Blocks.glass.getDefaultState();
            world.setBlockState(new BlockPos(x, target_h, z), bs);
            for (int y = target_h + 1; y < h; y++) {
                //world.setBlockToAir(new BlockPos(x, y, z));
                if (!chunk.getBlock(new BlockPos(x, y, z)).isAir(null, null)) {
                    world.setBlockState(new BlockPos(x, y, z), glass);
                }
            }
        } else if (h < target_h) {
            // need to fill
            Block b = chunk.getBlock(new BlockPos(x, h, z));
            IBlockState bs = fillBlock; // Blocks.diamond_block.getDefaultState();
            for (int y = h; y <= target_h; y++) {
                world.setBlockState(new BlockPos(x, y, z), bs);
            }
        } else {
            // check for water
            IBlockState bs = sameBlock; // Blocks.lapis_block.getDefaultState();
            world.setBlockState(new BlockPos(x, target_h, z), bs);
        }
    }
    void bullDoze(float r_tower, float r_grounds, float r_slope, World world, IChunkProvider chunkProvider) {
        System.out.print("tower: " + r_tower + ", grounds: " + r_grounds + ", slope: "+ r_slope);
        int px = _pos.getX();
        int pz = _pos.getZ();
        int py = _pos.getY();

        int x0 = px - (int)r_slope;
        int x1 = px + (int)r_slope;
        int z0 = pz - (int)r_slope;
        int z1 = pz + (int)r_slope;

        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                int r = dist(x, z, px, pz);
                if (r < r_tower){
                    setTerrainHeight(x, py, z, world, chunkProvider, Blocks.stonebrick.getDefaultState(), Blocks.stonebrick.getDefaultState(), Blocks.stonebrick.getDefaultState());
                } else if (r < r_grounds) {
                    setTerrainHeight(x, py, z, world, chunkProvider, Blocks.sandstone.getDefaultState(), Blocks.sandstone.getDefaultState(), Blocks.sandstone.getDefaultState());
                } else if (r < r_slope){
                    double pct = 1.0 - (r_slope - r) / (r_slope - r_grounds);
                    int ground = HeightAnalyser.get_ground_level(new BlockPos(x, py, z), chunkProvider);
                    int slope_y = py + (int)Math.round((ground - py) * pct);
                    setTerrainHeight(x, slope_y, z, world, chunkProvider, Blocks.emerald_block.getDefaultState(), Blocks.redstone_block.getDefaultState(), Blocks.lapis_block.getDefaultState());
                }
            }
        }
        world.setBlockState(_pos.up(), Blocks.stained_hardened_clay.getDefaultState());
    }

    void build(World world, IChunkProvider chunkProvider) {
        bullDoze((float)TOWER_SIZE/2.0f, (float)GROUNDS_SIZE/2.0f, (float)SLOPE_SIZE/2.0f, world, chunkProvider);
    }
}
