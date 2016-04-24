/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import com.google.common.collect.Maps;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.ivtoolkit.tools.MCRegistryDefault;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import java.util.Map;

/**
 * Created by lukas on 04.09.15.
 */
public class FMLRemapper implements MCRegistry
{
    protected String domain;

    protected MCRegistry parent = new MCRegistryDefault();

    protected Map<String, Block> blockRemaps = Maps.newHashMap();
    protected Map<String, Item> itemRemaps = Maps.newHashMap();

    public FMLRemapper(String domain, MCRegistry parent)
    {
        this.parent = parent;
        this.domain = domain;
    }

    public void registerLegacyIDs(Block block, boolean inferItem, String... oldIDs)
    {
        for (String oldID : oldIDs)
        {
            String fullID = String.format("%s:%s", domain, oldID);

            blockRemaps.put(fullID, block);
            if (inferItem)
                itemRemaps.put(fullID, parent.itemFromID(parent.idFromBlock(block)));
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

    @Override
    public Item itemFromID(String itemID)
    {
        Item item = itemRemaps.get(itemID);
        return item != null ? item : parent.itemFromID(itemID);
    }

    @Override
    public String idFromItem(Item item)
    {
        return parent.idFromItem(item);
    }

    @Override
    public void modifyItemStackCompound(NBTTagCompound compound, String itemID)
    {
        parent.modifyItemStackCompound(compound, itemID);
    }

    @Override
    public Block blockFromID(String blockID)
    {
        Block block = blockRemaps.get(blockID);
        return block != null ? block : parent.blockFromID(blockID);
    }

    @Override
    public String idFromBlock(Block block)
    {
        return parent.idFromBlock(block);
    }

    @Override
    public TileEntity loadTileEntity(NBTTagCompound compound)
    {
        return parent.loadTileEntity(compound);
    }
}
