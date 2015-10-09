package com.markyshouse.mc.Castella;

import com.markyshouse.mc.*;
import net.minecraft.block.*;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.village.Village;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.structure.MapGenStructureData;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.common.IPlantable;

import java.util.*;

/**
 * Created by mark on 9/16/2015.
 */
public abstract class StructureBuilder {
    protected ITemplateBlockChooser blockChooser;

    protected IBlockChooser wallChooser;
    protected IBlockChooser floorChooser;
    protected IBlockChooser windowChooser;
    protected IBlockChooser doorChooser;
    protected IBlockChooser slopeChooser;
    protected IBlockChooser groundsChooser;

    public void setTemplateBlockChooser(ITemplateBlockChooser chooser) { this.blockChooser = chooser; }

    public void setWallChooser(IBlockChooser wallChooser) { this.wallChooser = wallChooser; }
    public void setFloorChooser(IBlockChooser floorChooser) { this.floorChooser = floorChooser; }
    public void setWindowChooser(IBlockChooser windowChooser) {
        this.windowChooser = windowChooser;
    }
    public void setDoorChooser(IBlockChooser doorChooser) {
        this.doorChooser = doorChooser;
    }
    public void setSlopeChooser(IBlockChooser slopeChooser) {
        this.slopeChooser = slopeChooser;
    }
    public void setGroundsChooser(IBlockChooser groundsChooser) {
        this.groundsChooser = groundsChooser;
    }

    boolean flip_x;
    boolean flip_z;
    enum ROTATION {ROT_0, ROT_90, ROT_180, ROT_270};
    ROTATION rotation;

    public char[][] footprint;

    public static final int TOWER_TYPE = 1;
    public static final int INTERSECTION_TYPE = 2;
    abstract public int getType();

    public boolean biomeIsInList(BiomeGenBase biome, List biome_list) {
        return biome_list.contains(biome);
    }
    public Structure tryToBuildHere(int x, int z, Random random, World world, IChunkProvider chunkProvider) {
        if (random.nextFloat() > 0.5) {
            return null;
        }
        List goodBiomes = getBuildableBiomeList();

        char[][] footprint = getFootprint(getType());
        int h = footprint.length;
        int w = footprint[0].length;

        int min_h = 0;
        int max_h = 0;
        int JITTER_X = w/2;
        int JITTER_Z = h/2;
        boolean found = false;
        //System.out.println(String.format("TRY TO BUILD at %d, %d", x, z));
        for (int jitter_x = 0; !found && jitter_x <= 2; jitter_x++) {
            for (int jitter_z = 0; !found && jitter_z <= 2; jitter_z++) {
                int jx = x + (jitter_x * JITTER_X);
                int jz = z + (jitter_z * JITTER_Z);
                BlockPos try_pos = new BlockPos(jx, 64, jz);
                TerrainMap map = new TerrainMap(try_pos, w, h, world, chunkProvider);
                if (!map.isLevelEnough())
                    return null;

                List<BiomeGenBase> biomeList = map.getBiomeList();
                if (biomeList != null) {
                    for (BiomeGenBase b : biomeList) {
                        if (! biomeIsInList(b, goodBiomes)) {
                            //System.out.println(String.format("-- bad biome: %s", b.biomeName));
                            return null;
                        }
                    }
                }
                if (!containsStructure(jx, jz, w, h, world, chunkProvider)) {
                    Structure structure = build(new BlockPos(jx, map.getBuildHeight(), jz), map, random, world, chunkProvider);
                    if (structure != null) {
                        //System.out.println("** location jitter: " + jx + ", " + jz + " -- " + map.getBuildHeight() + " " + "biome.biomeName" + " : " + (int) Math.floor(jx / 16.0) + ", " + (int) Math.floor(jz / 16.0));
                        //System.out.println(String.format("   bounds: (%d, %d) - (%d, %d)",
                        //        structure.territoryBox.minX, structure.territoryBox.minZ, structure.territoryBox.maxX, structure.territoryBox.maxZ));
                    }
                    return structure;
                }
            }
        }
        return null;
    }
    abstract protected List getBuildableBiomeList();

    static protected boolean containsStructure(int bx, int bz, int bw, int bh, World world, IChunkProvider chunkProvider) {
        // TODO - use village collection.
        MapStorage storage = world.getPerWorldStorage();
        VillageCollection villageCollection = (VillageCollection)storage.loadData(MarkyshouseWorldSavedData.class, "villages");
        Village v = villageCollection.getNearestVillage(new BlockPos(bx, 64, bz), 300);
        if (v != null) {
            int r = v.getVillageRadius();
            BlockPos center = v.getCenter();
            if (!(center.getX() + r < bx || center.getX() - r > bx + bw || center.getZ() + r < bz || center.getZ() - r > bz + bw)) {
                System.out.println("Too Close to Village at " + center.toString());
                return true;
            }
        }
        // Temples too?
        MapGenStructureData temples = (MapGenStructureData)storage.loadData(MapGenStructureData.class, "Temple");
        NBTTagCompound tmpls = temples.func_143041_a();
        Iterator it = tmpls.getKeySet().iterator();
        while (it.hasNext()) {
            String k = (String)it.next();
            NBTTagCompound t = tmpls.getCompoundTag(k);
            int[] tbb = t.getIntArray("BB");
            if (tbb[3] < bx || tbb[0] > bx + bw || tbb[5] < bz || tbb[2] > bz + bh) continue;
            System.out.println(String.format("Too close to Temple, BB : %s", tbb.toString()));
            return true;
        }
        return false;
    }


    abstract void init(Random random);

    private static final int[] search_pattern = new int[]{0, -512, 512};

    public Structure findClosestStructure(BlockPos position, World world) {
        Structure structure = null;

        MarkyshouseWorldSavedData data = MarkyshouseWorldSavedData.forWorld(world);

        double myX = position.getX();
        double myZ = position.getZ();

        NBTTagCompound nearest = null;
        double distance = 1000000000.0; // arbitrariliy large number
        for (int xoff = 0; xoff < 3; xoff++) {
            for (int zoff = 0; zoff < 3; zoff++) {
                int rx = position.getX() + search_pattern[xoff];
                int rz = position.getZ() + search_pattern[zoff];
                NBTTagCompound regionData = data.getRegionData(rx, rz);

                if (regionData != null) {
                    // FIND NEAREST NEIGHBOR
                    NBTTagCompound structureMap = regionData.getCompoundTag("structuremap");
                    Set keyset = structureMap.getKeySet();
                    Iterator it = keyset.iterator();
                    while (it.hasNext()) {
                        String key = (String) it.next();
                        NBTTagCompound s = structureMap.getCompoundTag(key);
                        if (s.hasKey("position")) {
                            // make sure other is not 'this'
                            // make sure it has road slots open
                            boolean hasRoads = false;
                            if (s.hasKey("roadpoints")) {
                                NBTTagList roads = s.getTagList("roadpoints", s.getId());
                                for (int j = 0; j < roads.tagCount(); j++) {
                                    NBTTagCompound road = roads.getCompoundTagAt(j);
                                    if (!road.getBoolean("inuse")) {
                                        hasRoads = true;
                                        break;
                                    }
                                }
                            }
                            if (!hasRoads) continue;

                            int[] t = s.getIntArray("position");
                            double d = Math.sqrt((myX - t[0]) * (myX - t[0]) + (myZ - t[2]) * (myZ - t[2]));
                            if (d < distance) {
                                distance = d;
                                nearest = s;
                            }
                        }
                    }
                }
            }
        }
        // TODO pick a meaningful maximum
        if (nearest != null && distance < 512.0) {
            structure = new Structure(new StructureBoundingBox2D(nearest.getIntArray("territory")));

            int[] p = nearest.getIntArray("position");
            structure.position = new BlockPos(p[0], p[1], p[2]);
            structure.StructureType = nearest.getInteger("type");
            if (nearest.hasKey("roadpoints")) {
                NBTTagList rpoints = nearest.getTagList("roadpoints", nearest.getId());
                for (int i = 0; i < rpoints.tagCount(); i++) {
                    NBTTagCompound rpTag = rpoints.getCompoundTagAt(i);
                    int[] rpos = rpTag.getIntArray("position");
                    boolean inUse = rpTag.getBoolean("inuse");
                    BlockPos rPos = new BlockPos(rpos[0], rpos[1], rpos[2]);
                    structure.setRoadPoint(EnumFacing.getHorizontal(i), rPos, inUse);
                }
            }

        }
        return structure;
    }
    public Structure build(BlockPos position, TerrainMap map, Random random, World world, IChunkProvider chunkProvider) {
        //int[][] tree_map = TerrainManager.getTreeMap(position, footprint[0].length, footprint.length, world, chunkProvider);
        StructureBoundingBox2D territoryBox = getTerritoryBox(position);

        // Make sure it's not too close to existing structures
        MarkyshouseWorldSavedData data = MarkyshouseWorldSavedData.forWorld(world);
        for (int xoff = 0; xoff < 3; xoff++) {
            for (int zoff = 0; zoff < 3; zoff++) {
                NBTTagCompound regionData =
                        data.getRegionData(position.getX() + search_pattern[xoff], position.getZ() + search_pattern[zoff]);

                if (regionData != null) {
                    NBTTagCompound structureMap = regionData.getCompoundTag("structuremap");
                    Set keyset = structureMap.getKeySet();
                    Iterator it = keyset.iterator();
                    while (it.hasNext()) {
                        String key = (String) it.next();
                        NBTTagCompound s = structureMap.getCompoundTag(key);
                        if (s.hasKey("territory")) {
                            int[] t = s.getIntArray("territory");
                            StructureBoundingBox2D box = new StructureBoundingBox2D(t);
                            if (territoryBox.intersects(box)) {
                                // System.out.println(String.format("-- TOO CLOSE TO existing Structure at (%d, %d) - (%d, %d)", box.minX, box.minZ, box.maxX, box.maxZ));
                                return null;
                            }
                        }
                    }
                }
            }
        }
        Structure structure = new Structure(territoryBox);
        buildInner(structure, position, map, random, world, chunkProvider);
        structure.StructureType = this.getType();

        Structure neighbor = findClosestStructure(position, world);
        boolean road_built = false;
        if (neighbor != null) {
            RoadBuilder roadBuilder = new RoadBuilder();
            road_built = roadBuilder.buildRoad(structure, neighbor, random, world, chunkProvider);
        }

        // WRITE UPDATED DATA
        int[] territory_array = new int[]{territoryBox.minX, territoryBox.minZ, territoryBox.maxX, territoryBox.maxZ};
        int[] position_array = new int[]{structure.position.getX(),structure.position.getY(),structure.position.getZ()};

        int rx = structure.position.getX();
        int rz = structure.position.getZ();

        NBTTagCompound regionData = data.getRegionData(rx, rz);
        NBTTagCompound structureMap = regionData.getCompoundTag("structuremap");

        NBTTagCompound structure_tag = new NBTTagCompound();
        structure_tag.setIntArray("territory", territory_array);
        structure_tag.setIntArray("position", position_array);
        structure_tag.setInteger("type", structure.StructureType);
        if (structure.roadPoints.length > 0) {
            NBTTagList rpList = new NBTTagList();
            for (Structure.RoadPoint rp : structure.roadPoints) {
                int[] p = new int[]{rp.position.getX(),rp.position.getY(),rp.position.getZ()};
                boolean inUse = rp.isInUse();
                NBTTagCompound rpTag = new NBTTagCompound();
                rpTag.setIntArray("position", p);
                rpTag.setBoolean("inuse", inUse);
                rpList.appendTag(rpTag);
            }
            structure_tag.setTag("roadpoints", rpList);
        }

        structureMap.setTag(String.format("%d:%d:%d",position_array[0], position_array[1],position_array[2]), structure_tag);
        regionData.setTag("structuremap", structureMap);

        if (road_built) {
            rx = neighbor.position.getX();
            rz = neighbor.position.getZ();
            regionData = data.getRegionData(rx, rz);
            structureMap = regionData.getCompoundTag("structuremap");
            NBTTagCompound neighbor_tag =
                    structureMap.getCompoundTag(String.format("%d:%d:%d",neighbor.position.getX(), neighbor.position.getY(), neighbor.position.getZ()));
            if(neighbor_tag != null) {
                // fill it in from neighbor.
                NBTTagList rpList = new NBTTagList();
                for (Structure.RoadPoint rp : neighbor.roadPoints) {
                    int[] p = new int[]{rp.position.getX(),rp.position.getY(),rp.position.getZ()};
                    boolean inUse = rp.isInUse();
                    NBTTagCompound rpTag = new NBTTagCompound();
                    rpTag.setIntArray("position", p);
                    rpTag.setBoolean("inuse", inUse);
                    rpList.appendTag(rpTag);
                }
                neighbor_tag.setTag("roadpoints", rpList);
            }
            regionData.setTag("structuremap", structureMap);
        }

        data.markDirty();
        return structure;
    }

    abstract public StructureBoundingBox2D getTerritoryBox(BlockPos origin);

    abstract public void buildInner(Structure structure, BlockPos origin, TerrainMap map, Random random, World world, IChunkProvider chunkProvider);
    public int getWidth() {
        return footprint[0].length;
    }
    public int getHeight() {
        return footprint.length;
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

    private static char[][] getFootprint(int t) {
        switch (t) {
            case TOWER_TYPE:
                return TowerBuilder.getFootprint();
            case INTERSECTION_TYPE:
                return IntersectionBuilder.getFootprint();
        }
        return null;
    }
}
