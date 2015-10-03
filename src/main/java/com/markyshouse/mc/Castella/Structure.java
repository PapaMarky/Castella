package com.markyshouse.mc.Castella;

import com.markyshouse.mc.StructureBoundingBox2D;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mark on 9/23/2015.
 */
public class Structure {
    // places where a road can connect to the structure
    public class RoadPoint {
        BlockPos position = null;
        boolean inUse = false;

        public RoadPoint(BlockPos p) {
            position = p;
        }

        public BlockPos getPosition() { return position; }
        public boolean isInUse() { return inUse; }
        public void setUsed() { inUse = true; }
    }
    // Bounds
    public StructureBoundingBox2D territoryBox;
    public BlockPos position;
    public int StructureType = 0;
    public ArrayList<RoadPoint> roadPoints = new ArrayList<RoadPoint>();

    public Structure(StructureBoundingBox2D territory) {
        territoryBox = territory;
    }
    public void addRoadPoint(BlockPos pos) {
        roadPoints.add(new RoadPoint(pos));
    }
    public void addRoadPoint(BlockPos pos, boolean inuse) {
        RoadPoint rp = new RoadPoint(pos);
        rp.inUse = inuse;
        roadPoints.add(rp);
    }
}
