package com.markyshouse.mc.Castella;

import com.markyshouse.mc.Castella.Castella;
import com.markyshouse.mc.IBlockChooser;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

import java.util.HashMap;

/**
 * Created by mark on 9/20/2015.
 */
public class TemplateBlockChooser implements ITemplateBlockChooser {
    protected HashMap<Character, IBlockChooser> map = new HashMap<Character, IBlockChooser>();

    public IBlockChooser chooseBlock(BlockPos pos, char template_value, World world, IChunkProvider chunkProvider) {
        if (map.containsKey(template_value)) {
            return map.get(template_value);
        }
        return null;
    }
    public boolean add_chooser(char c, IBlockChooser chooser) {
        if (map.containsKey(c)) {
            return false;
        }
        map.put(c, chooser);
        return true;
    }
}
