package com.markyshouse.mc;

import net.minecraft.world.gen.structure.StructureBoundingBox;

/**
 * Created by mark on 9/29/2015.
 */
public class StructureBoundingBox2D {
    /** The first x coordinate of a bounding box. (west) */
    public int minX;
    /** The first z coordinate of a bounding box. (north) */
    public int minZ;
    /** The second x coordinate of a bounding box. (east) */
    public int maxX;
    /** The second z coordinate of a bounding box. (south) */
    public int maxZ;

    public StructureBoundingBox2D(int[] array)
    {
        if (array.length == 4)
        {
            this.minX = array[0];
            this.minZ = array[1];
            this.maxX = array[2];
            this.maxZ = array[3];
        }
    }
    public StructureBoundingBox2D(int x0, int z0, int x1, int z1)
    {
        this.minX = x0;
        this.minZ = z0;
        this.maxX = x1;
        this.maxZ = z1;
    }

    public StructureBoundingBox2D(StructureBoundingBox other)
    {
        this.minX = other.minX;
        this.minZ = other.minZ;
        this.maxX = other.maxX;
        this.maxZ = other.maxZ;
    }
    public boolean intersects(StructureBoundingBox2D other) {
        if (other.maxZ < minZ || other.minZ > maxZ) {
            return false;
        }
        if (other.maxX < minX || other.minX > maxX) {
            return false;
        }
        return true;
    }
    /**
     * Discover if bounding box can fit within the current bounding box object.
     */
    public boolean contains(StructureBoundingBox2D other)
    {
        return this.maxX >= other.minX && this.minX <= other.maxX && this.maxZ >= other.minZ && this.minZ <= other.maxZ;
    }

    /**
     * Discover if a coordinate is inside the bounding box area.
     */
    public boolean contains(int x0, int z0, int x1, int z1)
    {
        return this.maxX >= x0 && this.minX <= x1 && this.maxZ >= z0 && this.minZ <= z1;
    }

}
