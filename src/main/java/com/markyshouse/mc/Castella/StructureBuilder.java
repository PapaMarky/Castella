package com.markyshouse.mc.Castella;

import com.markyshouse.mc.*;
import net.minecraft.block.*;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.village.Village;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.structure.MapGenStructureData;
import net.minecraft.world.storage.MapStorage;

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

    public boolean canBuildHere(int x, int z, Random random, World world, IChunkProvider chunkProvider) {
        /*
        // TODO: randomly pick a type. Maybe loop trying several.
        // Right now there is only one type
        TYPE type = TYPE.TOWER;
        BlockPos try_pos = new BlockPos(x, 64, z);
        Chunk chunk = chunkProvider.provideChunk(try_pos);
        BiomeGenBase biome = chunk.getBiome(try_pos, world.getWorldChunkManager());

        // skip places that are not structure friendly
        switch (biome.biomeID) {
            case 0: // ocean
                //case 3: // extreme hills
            case 7: // river
            case 8: // hell
            case 9: // sky
            case 10: // frozen ocean
            case 11: // frozen river
            //case 20: // extreme hills edge
            case 24: // deep ocean
            //case 29: // roofed forest
                return false;
        }
        char[][] footprint = getFootprint(type);
        int h = footprint.length;
        int w = footprint[0].length;

        int min_h = 0;
        int max_h = 0;
        int JITTER_X = w/2;
        int JITTER_Z = h/2;
        boolean found = false;
        //TerrainManager analyser = new TerrainManager(world, chunkProvider);
        for (int jitter_x = -1; !found && jitter_x <= 1; jitter_x++) {
            for (int jitter_z = -1; !found && jitter_z <= 1; jitter_z++) {
                int jx = x + (jitter_x * JITTER_X);
                int jz = z + (jitter_z * JITTER_Z);
                if (map.isLevelEnough() && !containsStructure(jx, jz, w, h, world, chunkProvider)) {
                    System.out.println("** location jitter: " + jx + ", " + jz + " -- " + map.build_height + " " + biome.biomeName + " : " + (int)Math.floor(jx/16.0) + ", " + (int) Math.floor(jz/16.0));
                    //return getBuilder(TYPE.TOWER, new BlockPos(jx, analyser.build_height, jz), random);
                    return true;
                }
            }
        }
        */
        return false;
    }

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

        /*
        for (int i = 0; i < bw; i++) {
            for (int j = 0; j < bh; j++) {
                BlockPos pos = new BlockPos(bx + i, 64, bz + j);
                Chunk chunk = chunkProvider.provideChunk(pos);
                int h = chunk.getHeight(pos);
                pos = new BlockPos(bx + i, h, bz +j);
                Block b = chunk.getBlock(pos);
                int z = h;
                BlockState bs = b.getBlockState();
                IBlockState ibs = bs.getBaseState();
                int id = Block.getIdFromBlock(b);

                // ids of blocks that only spawn in structures
                // see http://minecraft-ids.grahamedgecombe.com/
                switch(id) {
                    case 1:
                    case 4:
                    case 5:
                    case 7:
                    case 20:
                    case 43:
                    case 44:
                    case 45:
                    case 46:
                    case 47:
                    case 50:
                    case 52:
                    case 53:
                    case 54:
                    case 58:
                    case 59:
                    case 60:
                    case 61:
                    case 62:
                    case 63:
                    case 64:
                    case 67:
                    case 85:
                    case 91:
                    case 95:
                    case 96:
                    case 98:
                    case 101:
                    case 102:
                    case 107:
                    case 108:
                    case 109:
                    case 116:
                    case 117:
                    case 118:
                    case 119:
                    case 125:
                    case 126:
                    case 128:
                    case 131:
                    case 132:
                    case 134:
                    case 135:
                    case 136:
                    case 139:
                    case 140:
                    case 141:
                    case 142:
                    case 143:
                    case 160:
                    case 163:
                    case 164:
                    case 167:
                    case 171:
                    case 176:
                    case 177:
                    case 180:
                    case 181:
                    case 182:
                    case 183:
                    case 184:
                    case 185:
                    case 186:
                    case 187:
                    case 188:
                    case 189:
                    case 190:
                    case 191:
                    case 192:
                    case 193:
                    case 194:
                    case 195:
                    case 196:
                    case 197:
                    case 324:
                    case 328:
                        System.out.println("--- Found structure");
                        return true;
                }
            }
        }
        return false;
        */
    }


    abstract void init(Random random);
    public Structure findClosestStructure(BlockPos position, World world) {
        Structure structure = null;

        MarkyshouseWorldSavedData data = MarkyshouseWorldSavedData.forWorld(world);
        NBTTagCompound regionData = data.getRegionData(position.getX(), position.getZ());
        NBTTagCompound nearest = null;
        double distance = 1000000000.0; // arbitrariliy large number

        double myX = position.getX();
        double myZ = position.getZ();

        if (regionData != null) {
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
                            if (! road.getBoolean("inuse")) {
                                hasRoads = true;
                                break;
                            }
                        }
                    }
                    if (!hasRoads) continue;

                    int[] t = s.getIntArray("position");
                    double d = Math.sqrt( (myX-t[0])*(myX-t[0]) + (myZ-t[2])*(myZ-t[2]));
                    if (d < distance) {
                        distance = d;
                        nearest = s;
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
                    structure.addRoadPoint(rPos, inUse);
                }
            }

        }
        return structure;
    }
    public Structure build(BlockPos position, TerrainMap map, Random random, World world, IChunkProvider chunkProvider) {
        //int[][] tree_map = TerrainManager.getTreeMap(position, footprint[0].length, footprint.length, world, chunkProvider);

        // Make sure it's not too close to existing structures
        MarkyshouseWorldSavedData data = MarkyshouseWorldSavedData.forWorld(world);
        NBTTagCompound regionData = data.getRegionData(position.getX(), position.getZ());

        StructureBoundingBox2D territoryBox = getTerritoryBox(position);
        if (regionData != null) {
            NBTTagCompound structureMap = regionData.getCompoundTag("structuremap");
            Set keyset = structureMap.getKeySet();
            Iterator it = keyset.iterator();
            while (it.hasNext()) {
                String key = (String)it.next();
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
        Structure structure = new Structure(territoryBox);
        buildInner(structure, position, map, random, world, chunkProvider);
        structure.StructureType = this.getType();

        Structure neighbor = findClosestStructure(position, world);

        // Add new structure to all regions it intersects
        int rx0 = (int)Math.floor(Math.floor(territoryBox.minX/16.0 / 32.0));
        int rz0 = (int)Math.floor(Math.floor(territoryBox.minZ/16.0 / 32.0));
        int rx1 = (int)Math.floor(Math.floor(territoryBox.maxX/16.0 / 32.0));
        int rz1 = (int)Math.floor(Math.floor(territoryBox.maxZ/16.0 / 32.0));

        HashMap<String, Boolean> rmap = new HashMap<String, Boolean>();
        int[] territory_array = new int[]{territoryBox.minX, territoryBox.minZ, territoryBox.maxX, territoryBox.maxZ};
        int[] position_array = new int[]{position.getX(),position.getY(),position.getZ()};
        for (int rx = rx0; rx <= rx1; rx++) {
            for (int rz = rz0; rz <= rz1; rz++) {
                String key = data.getRegionKey(rx, rz);
                if (!rmap.containsKey(key)) {
                    rmap.put(key, true);
                    regionData = data.getRegionDataFromRegion(rx, rz);
                    NBTTagCompound structureMap = regionData.getCompoundTag("structuremap");

                    NBTTagCompound structure_tag = new NBTTagCompound();
                    structure_tag.setIntArray("territory", territory_array);
                    structure_tag.setIntArray("position", position_array);
                    structure_tag.setInteger("type", structure.StructureType);
                    if (structure.roadPoints.size() > 0) {
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
                }
            }
        }
        data.markDirty();
        return structure;
    }

    abstract public StructureBoundingBox2D getTerritoryBox(BlockPos origin);

    abstract public void buildInner(Structure structure, BlockPos origin, TerrainMap map, Random random, World world, IChunkProvider chunkProvider);

    private static StructureBuilder getBuilder(int t, BlockPos blockPos, Random random) {
        switch (t) {
            case TOWER_TYPE:
                // should be a static function on TowerBuilder that picks type from blockPos, etc
                return null; // new TowerBuilder(TowerBuilder.TYPE.STONE, blockPos, random);
        }
        return null;
    }
    private static char[][] getFootprint(int t) {
        switch (t) {
            case TOWER_TYPE:
                return TowerBuilder.getFootprint();
        }
        return null;
    }
}
