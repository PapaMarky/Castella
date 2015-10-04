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
    public RoadPoint[] roadPoints = new RoadPoint[4];

    public Structure(StructureBoundingBox2D territory) {
        territoryBox = territory;
    }
    public void setRoadPoint(EnumFacing direction, BlockPos pos) {
        RoadPoint rp = new RoadPoint(pos);
        setRoadPoint(direction, rp);
    }
    public void setRoadPoint(EnumFacing direction, BlockPos pos, boolean inuse) {
        RoadPoint rp = new RoadPoint(pos);
        rp.inUse = inuse;
        setRoadPoint(direction, rp);
    }
    private void setRoadPoint(EnumFacing direction, RoadPoint rp) {
        if (direction != null && direction.getAxis().isHorizontal()) {
            roadPoints[direction.getHorizontalIndex()] = rp;
        }
    }

    // Select a RoadPoint in the direction of the Structure, other.
    // Return null if the appropriate RoadPoint is in use
    public RoadPoint selectRoadPoint(Structure other) {
        EnumFacing direction = EnumFacing.getFacingFromVector(
                other.position.getX() - this.position.getX(),
                0,
                other.position.getZ() - this.position.getZ());
        if (direction.getAxis().isHorizontal() && !roadPoints[direction.getHorizontalIndex()].isInUse()) {
            return roadPoints[direction.getHorizontalIndex()];
        }

        return null;
    }
    // Select a RoadPoint in the direction of angle.
    // Return null if the appropriate RoadPoint is in use
    public RoadPoint selectRoadPoint(double angle) {
        return null;
    }
    // NOTE - Angle is arccos of two vectors
    public RoadPoint getRoadPoint(EnumFacing direction) {
        if (direction != null && direction.getAxis().isHorizontal())
            return roadPoints[direction.getHorizontalIndex()];
        return null;
    }
}
