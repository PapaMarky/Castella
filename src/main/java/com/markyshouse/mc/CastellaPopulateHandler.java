package com.markyshouse.mc;

import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by mark on 9/20/2015.
 */
public class CastellaPopulateHandler {

    @SubscribeEvent(priority= EventPriority.NORMAL, receiveCanceled=true)
    public void onEvent(PopulateChunkEvent.Pre event) {
        //System.out.println(String.format("PopulateChunkEvent Pre %2d, %2d : %s",event.chunkX, event.chunkZ, event.hasVillageGenerated ? "true" : "false"));
    }
    @SubscribeEvent(priority= EventPriority.NORMAL, receiveCanceled=true)
    public void onEvent(PopulateChunkEvent.Populate event) {
        System.out.println(String.format("PopulateChunkEvent Populate %2d, %2d : %s",event.chunkX, event.chunkZ, event.hasVillageGenerated ? "true" : "false"));
    }
    @SubscribeEvent(priority= EventPriority.NORMAL, receiveCanceled=true)
    public void onEvent(PopulateChunkEvent.Post event) {
        //System.out.println(String.format("PopulateChunkEvent Post %2d, %2d : %s",event.chunkX, event.chunkZ, event.hasVillageGenerated ? "true" : "false"));
    }
}
