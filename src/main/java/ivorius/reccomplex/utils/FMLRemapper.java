/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import com.google.common.collect.Maps;
import ivorius.ivtoolkit.tools.MCRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

import java.util.Map;

/**
 * Created by lukas on 25.04.16.
 */
public class FMLRemapper
{
    protected String domain;

    protected final Map<String, String> blockRemaps = Maps.newHashMap();
    protected final Map<String, String> itemRemaps = Maps.newHashMap();
    protected final Map<String, String> tileEntityRemaps = Maps.newHashMap();

    public FMLRemapper(String domain)
    {
        this.domain = domain;
    }

    public void registerLegacyBlockIDs(String blockID, boolean inferItem, String... oldIDs)
    {
        for (String oldID : oldIDs)
        {
            String fullID = String.format("%s:%s", domain, oldID);

            blockRemaps.put(fullID, blockID);
            if (inferItem)
                itemRemaps.put(fullID, blockID);
        }
    }

    public void registerLegacyItemIDs(String itemID, String... oldIDs)
    {
        for (String oldID : oldIDs)
            itemRemaps.put(String.format("%s:%s", domain, oldID), itemID);
    }

    public void registerLegacyTileEntityIDs(String tileEntityID, String... oldIDs)
    {
        for (String oldID : oldIDs)
            tileEntityRemaps.put(String.format("%s:%s", domain, oldID), tileEntityID);
    }

    public String mapBlock(String id)
    {
        String remap = blockRemaps.get(id);
        return remap != null ? remap : id;
    }

    public String remapBlock(String id)
    {
        return blockRemaps.get(id);
    }

    public String mapItem(String id)
    {
        String remap = itemRemaps.get(id);
        return remap != null ? remap : id;
    }

    public String remapItem(String id)
    {
        return itemRemaps.get(id);
    }

    public String mapTileEntity(String id)
    {
        String remap = tileEntityRemaps.get(id);
        return remap != null ? remap : id;
    }

    public String remapTileEntity(String id)
    {
        return tileEntityRemaps.get(id);
    }

}
