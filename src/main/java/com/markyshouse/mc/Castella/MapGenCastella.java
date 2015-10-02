package com.markyshouse.mc.Castella;

import net.minecraft.util.MathHelper;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureStart;

import java.util.*;

/**
 * Created by mark on 9/26/2015.
 */
public class MapGenCastella extends MapGenStructure {
    private int field_82665_g;
    private int field_82666_h;
    /** World terrain type, 0 for normal, 1 for flat map */
    private int terrainType;

    public MapGenCastella()
    {
        this.field_82665_g = 32;
        this.field_82666_h = 8;
    }

    public MapGenCastella(Map p_i2093_1_)
    {
        this();
        Iterator iterator = p_i2093_1_.entrySet().iterator();

        while (iterator.hasNext())
        {
            Map.Entry entry = (Map.Entry)iterator.next();

            if (((String)entry.getKey()).equals("size"))
            {
                this.terrainType = MathHelper.parseIntWithDefaultAndMax((String) entry.getValue(), this.terrainType, 0);
            }
            else if (((String)entry.getKey()).equals("distance"))
            {
                this.field_82665_g = MathHelper.parseIntWithDefaultAndMax((String)entry.getValue(), this.field_82665_g, this.field_82666_h + 1);
            }
        }
    }
    public String getStructureName() { return "Castella";}
    /** A list of all the biomes towers can spawn in. */
    public static List villageSpawnBiomes = Arrays.asList(new BiomeGenBase[]{
            BiomeGenBase.plains, BiomeGenBase.desert, BiomeGenBase.savanna, BiomeGenBase.beach,
            BiomeGenBase.birchForest, BiomeGenBase.birchForestHills, BiomeGenBase.coldBeach, BiomeGenBase.coldTaiga,
            BiomeGenBase.coldTaigaHills, BiomeGenBase.desertHills, BiomeGenBase.extremeHills, BiomeGenBase.extremeHillsEdge,
            BiomeGenBase.forest, BiomeGenBase.forestHills, BiomeGenBase.jungle, BiomeGenBase.jungleEdge, BiomeGenBase.jungleHills,
            BiomeGenBase.mesa, BiomeGenBase.mesaPlateau, BiomeGenBase.roofedForest, BiomeGenBase.savannaPlateau, BiomeGenBase.stoneBeach,
            BiomeGenBase.swampland, BiomeGenBase.taiga, BiomeGenBase.taigaHills
    });
    protected boolean canSpawnStructureAtCoords(int p_75047_1_, int p_75047_2_){
        int k = p_75047_1_;
        int l = p_75047_2_;

        if (p_75047_1_ < 0)
        {
            p_75047_1_ -= this.field_82665_g - 1;
        }

        if (p_75047_2_ < 0)
        {
            p_75047_2_ -= this.field_82665_g - 1;
        }

        int i1 = p_75047_1_ / this.field_82665_g;
        int j1 = p_75047_2_ / this.field_82665_g;
        Random random = this.worldObj.setRandomSeed(i1, j1, 10387312);
        i1 *= this.field_82665_g;
        j1 *= this.field_82665_g;
        i1 += random.nextInt(this.field_82665_g - this.field_82666_h);
        j1 += random.nextInt(this.field_82665_g - this.field_82666_h);

        if (k == i1 && l == j1)
        {
            boolean flag = this.worldObj.getWorldChunkManager().areBiomesViable(k * 16 + 8, l * 16 + 8, 0, villageSpawnBiomes);

            if (flag)
            {
                return true;
            }
        }

        return false;
    }

    protected StructureStart getStructureStart(int p_75049_1_, int p_75049_2_){
        return null;
    }
    public static class Start extends StructureStart {

    }

    static {
        MapGenStructureIO.registerStructure(MapGenCastella.Start.class, "Castella");
    }
}
