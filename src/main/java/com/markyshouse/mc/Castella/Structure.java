package com.markyshouse.mc.Castella;

import com.markyshouse.mc.StructureBoundingBox2D;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.gen.structure.StructureBoundingBox;

/**
 * Created by mark on 9/23/2015.
 */
public class Structure {
    // Bounds
    StructureBoundingBox2D boundingBox;
    StructureBoundingBox2D territoryBox;
    EnumFacing facing;
    // Location
    BlockPos location; // redundant?
    // Type
    // SubType
    public Structure(StructureBoundingBox2D territory) {
        territoryBox = territory;
    }
}
