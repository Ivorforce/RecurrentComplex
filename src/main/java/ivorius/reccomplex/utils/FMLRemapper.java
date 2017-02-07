/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import com.google.common.collect.Maps;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Created by lukas on 25.04.16.
 */
public class FMLRemapper
{
    protected final Map<ResourceLocation, ResourceLocation> blockRemaps = Maps.newHashMap();
    protected final Map<ResourceLocation, ResourceLocation> itemRemaps = Maps.newHashMap();
    protected final Map<String, String> tileEntityRemaps = Maps.newHashMap();

    public void registerLegacyBlockIDs(ResourceLocation blockID, boolean inferItem, ResourceLocation... oldIDs)
    {
        for (ResourceLocation oldID : oldIDs)
        {
            blockRemaps.put(oldID, blockID);
            if (inferItem)
                itemRemaps.put(oldID, blockID);
        }
    }

    public void registerLegacyItemIDs(ResourceLocation itemID, ResourceLocation... oldIDs)
    {
        for (ResourceLocation oldID : oldIDs)
            itemRemaps.put(oldID, itemID);
    }

    public void registerLegacyTileEntityIDs(String tileEntityID, String... oldIDs)
    {
        for (String oldID : oldIDs)
            tileEntityRemaps.put(oldID, tileEntityID);
    }

    public ResourceLocation mapBlock(ResourceLocation id)
    {
        ResourceLocation remap = blockRemaps.get(id);
        return remap != null ? remap : id;
    }

    @Nullable
    public ResourceLocation remapBlock(ResourceLocation id)
    {
        return blockRemaps.get(id);
    }

    public ResourceLocation mapItem(ResourceLocation id)
    {
        ResourceLocation remap = itemRemaps.get(id);
        return remap != null ? remap : id;
    }

    @Nullable
    public ResourceLocation remapItem(ResourceLocation id)
    {
        return itemRemaps.get(id);
    }

    public String mapTileEntity(String id)
    {
        String remap = tileEntityRemaps.get(id);
        return remap != null ? remap : id;
    }

    @Nullable
    public String remapTileEntity(String id)
    {
        return tileEntityRemaps.get(id);
    }

}
