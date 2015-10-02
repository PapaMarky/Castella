package com.markyshouse.mc;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

import java.util.Map;
import java.util.Set;

/**
 * Created by mark on 9/28/2015.
 */
public class MarkyshouseWorldSavedData extends WorldSavedData {

    final static String key = "MarkysHouseData";
    private NBTTagCompound regionMap = null;
    public static MarkyshouseWorldSavedData forWorld(World world) {
        // Retrieves the MyWorldData instance for the given world, creating it if necessary
        MapStorage storage = world.getPerWorldStorage();
        MarkyshouseWorldSavedData result = (MarkyshouseWorldSavedData)storage.loadData(MarkyshouseWorldSavedData.class, key);
        if (result == null) {
            result = new MarkyshouseWorldSavedData();
            storage.setData(key, result);
        }
        return result;
    }
    public MarkyshouseWorldSavedData() {
        super(key);
    }
    public MarkyshouseWorldSavedData(String key) {
        super(key);
    }

    /**
     * Get the NBTTagCompound for the region corresponding to x, z in world coordinates
     * @param x World Coordinates
     * @param z World Coordinates
     * @return
     */
    public NBTTagCompound getRegionData(int x, int z) {
        int chunkX = (int)Math.floor(x/16.0);
        int chunkZ = (int)Math.floor(z/16.0);
        return getRegionDataFromChunk(chunkX, chunkZ);
    }

    public String getRegionKey(int regionX, int regionZ) {
        return String.format("%d:%d:region", regionX, regionZ);
    }
    public NBTTagCompound getRegionDataFromChunk(int chunkX, int chunkZ) {
        int regionX = (int)Math.floor(chunkX / 32.0);
        int regionZ = (int)Math.floor(chunkZ / 32.0);
        return getRegionDataFromRegion(regionX, regionZ);
    }
    public NBTTagCompound getRegionDataFromRegion(int regionX, int regionZ) {
        String regionKey = getRegionKey(regionX, regionZ);
        NBTTagCompound rMap = getRegionMap();
        NBTTagCompound regionData = null;
        if (rMap.hasKey(regionKey)) {
            regionData = rMap.getCompoundTag(regionKey);
        }else {
            regionData = new NBTTagCompound();
            rMap.setTag(regionKey, regionData);
            this.markDirty();
        }
        return regionData;
    }

    public void setRegionDataFromRegion(int regionX, int regionZ, NBTTagCompound map) {
        NBTTagCompound rMap = getRegionMap();
        rMap.setTag(getRegionKey(regionX, regionZ), map);
        this.markDirty();
    }
    /**
     * reads in data from the NBTTagCompound into this MapDataBase
     */
    public void readFromNBT(NBTTagCompound nbt) {
        System.out.println("READ!");
        regionMap = nbt.getCompoundTag("regionMap");
        boolean bh = nbt.getBoolean("BeenHere");
        if (bh) {
            System.out.print(" -- We've been here");
        }
    }

    private NBTTagCompound getRegionMap() {
        if (regionMap == null) {
            regionMap = new NBTTagCompound();
            this.markDirty();
        }
        return regionMap;
    }
    /**
     * write data to NBTTagCompound from this MapDataBase, similar to Entities and TileEntities
     */
    public void writeToNBT(NBTTagCompound nbt) {
        System.out.println("WRITE!");
        nbt.setTag("regionMap", getRegionMap());
        nbt.setBoolean("BeenHere", true);
    }
}