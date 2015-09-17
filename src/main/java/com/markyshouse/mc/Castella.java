package com.markyshouse.mc;

import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.common.config.Configuration;
import java.util.Random;

@Mod(modid = Castella.MODID, version = Castella.VERSION)
public class Castella
{
    public static final String MODID = "castella";
    public static final String VERSION = "1.0";

    private int buildings_per_region = 0;
    private int buildings_minimum_separation = 100;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();
        buildings_per_region = config.getInt("buildings_per_region", Configuration.CATEGORY_GENERAL, 20, 5, 100, "Number of structures in each 512x512 region");
        buildings_minimum_separation = config.getInt("buildings_minimum_separation", Configuration.CATEGORY_GENERAL, 75, 50, 200, "Minimum distance between structures");
        config.save();
    }
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
		// some example code
        System.out.println(" ******** Castella! ************ DIRT BLOCK >> "+Blocks.dirt.getUnlocalizedName());
        GameRegistry.registerWorldGenerator(new CastellaGenerator(buildings_per_region, buildings_minimum_separation), 0);
    }
}
