package com.markyshouse.mc.Castella;

import com.markyshouse.mc.CastellaPopulateHandler;
import com.markyshouse.mc.CastellaTerrainEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.common.config.Configuration;


// TODO study TerrainGen class, PopulateChunkEvent
// TODO event handling in general

@Mod(name = Castella.MODID, modid = Castella.MODID, version = Castella.VERSION, acceptableRemoteVersions = "*")
public class Castella
{
    public static final String MODID = "castella";
    public static final String VERSION = "1.0";

    private int buildings_per_region = 0;
    private int buildings_minimum_separation = 100;
    private int building_max_tries = 5;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();
        buildings_per_region = config.getInt("buildings_per_region", Configuration.CATEGORY_GENERAL, 20, 5, 100, "Number of structures in each 512x512 region");
        buildings_minimum_separation = config.getInt("buildings_minimum_separation", Configuration.CATEGORY_GENERAL, 75, 50, 512, "Minimum distance between structures");
        building_max_tries =
                config.getInt("building_max_tries", Configuration.CATEGORY_GENERAL, 5, 1, 50, "Maximum number of times we try to place each building");
        config.save();
    }
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        System.out.println(" ******** Castella! ************");
        GameRegistry.registerWorldGenerator(new CastellaGenerator(buildings_per_region, buildings_minimum_separation, building_max_tries), 0);
        MinecraftForge.EVENT_BUS.register(new CastellaPopulateHandler());
        //MinecraftForge.TERRAIN_GEN_BUS.register(new CastellaTerrainEventHandler());
    }
}
