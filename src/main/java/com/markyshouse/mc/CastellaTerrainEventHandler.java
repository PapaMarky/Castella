package com.markyshouse.mc;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockGlass;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.BiomeEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by mark on 9/20/2015.
 */
public class CastellaTerrainEventHandler {
    World world = null;
    @SubscribeEvent(priority= EventPriority.NORMAL, receiveCanceled=true)
    public void onEvent(BiomeEvent.GetVillageBlockID event) {
        if (event == null) return;
        //System.out.println(String.format("BiomeEvent.GetVillageBlockID: %s %s", event.biome == null ? "NULL" : event.biome.biomeName, event.original == null ? "NULL" : event.original.toString()));
/*        if (event.isCancelable() && !(event.original.getBlock() instanceof BlockAir)) {
            event.replacement = Blocks.gold_block.getDefaultState();
            event.setCanceled(true);
        }*/
    }
    @SubscribeEvent(priority= EventPriority.NORMAL, receiveCanceled=true)
    public void onEvent(BiomeEvent.CreateDecorator event) {
        System.out.println(String.format("BiomeEvent.CreateDecorator"));
    }
    @SubscribeEvent(priority= EventPriority.NORMAL, receiveCanceled=true)
    public void onEvent(BiomeEvent.BiomeColor event) {
        //System.out.println(String.format("BiomeEvent.BiomeColor : %s", event.biome.biomeName, event.newColor));
    }
    @SubscribeEvent(priority= EventPriority.NORMAL, receiveCanceled=true)
    public void onEvent(DecorateBiomeEvent.Decorate event) {
        this.world = event.world;
        //String biome = event.world.getChunkProvider().provideChunk(event.pos).getBiome(event.pos, null).biomeName;
        //System.out.println(String.format("DecorateBiomeEvent.Decorate: %s %s", biome, event.type));
    }
}
