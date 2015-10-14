/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lukas on 04.09.15.
 */
public class FMLRemapper
{
    private String domain;

    private Map<String, Block> blockRemaps = new HashMap<>();
    private Map<String, Item> itemRemaps = new HashMap<>();

    public FMLRemapper(String domain)
    {
        this.domain = domain;
    }

    public void registerLegacyIDs(Block block, boolean inferItem, String... oldIDs)
    {
        for (String oldID : oldIDs)
        {
            String fullID = String.format("%s:%s", domain, oldID);

            blockRemaps.put(fullID, block);
            if (inferItem)
                itemRemaps.put(fullID, Item.getItemFromBlock(block));
        }
    }

    public void registerLegacyIDs(Item item, String... oldIDs)
    {
        for (String oldID : oldIDs)
            itemRemaps.put(String.format("%s:%s", domain, oldID), item);
    }

    public void onMissingMapping(FMLMissingMappingsEvent event)
    {
        for (FMLMissingMappingsEvent.MissingMapping missingMapping : event.get())
        {
            switch (missingMapping.type)
            {
                case BLOCK:
                    if (blockRemaps.containsKey(missingMapping.name))
                        missingMapping.remap(blockRemaps.get(missingMapping.name));
                    break;
                case ITEM:
                    if (itemRemaps.containsKey(missingMapping.name))
                        missingMapping.remap(itemRemaps.get(missingMapping.name));
                    break;
            }
        }
    }
}
