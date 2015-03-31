/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import cpw.mods.fml.common.FMLLog;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.RecurrentComplex;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import org.apache.logging.log4j.Level;

/**
 * Created by lukas on 30.06.14.
 */
public class MCRegistrySpecial implements MCRegistry
{
    public static final MCRegistrySpecial INSTANCE = new MCRegistrySpecial();

    private final BiMap<String, Item> itemMap = HashBiMap.create();
    private final BiMap<String, Block> blockMap = HashBiMap.create();
    private final BiMap<String, Class<? extends TileEntity>> tileEntityMap = HashBiMap.create();

    public void register(String id, Item item)
    {
        itemMap.put(id, item);
    }

    public void register(String id, Block block)
    {
        blockMap.put(id, block);
    }

    public void register(String id, Class<? extends TileEntity> tileEntity)
    {
        tileEntityMap.put(id, tileEntity);
    }

    @Override
    public Item itemFromID(String itemID)
    {
        Item item = itemMap.get(itemID);
        return item != null ? item : (Item) Item.itemRegistry.getObject(itemID);
    }

    @Override
    public String idFromItem(Item item)
    {
        String id = itemMap.inverse().get(item);
        return id != null ? id : Item.itemRegistry.getNameForObject(item);
    }

    @Override
    public Block blockFromID(String blockID)
    {
        Block block = blockMap.get(blockID);
        return block != null ? block : Block.getBlockFromName(blockID);
    }

    @Override
    public String idFromBlock(Block block)
    {
        String id = blockMap.inverse().get(block);
        return id != null ? id : Block.blockRegistry.getNameForObject(block);
    }

    @Override
    public TileEntity loadTileEntity(NBTTagCompound compound)
    {
        // From TileEntity
        try
        {
            Class oclass = tileEntityMap.get(compound.getString("id"));

            if (oclass != null)
            {
                TileEntity tileEntity = (TileEntity) oclass.newInstance();
                tileEntity.readFromNBT(compound);
                return tileEntity;
            }
        }
        catch (Throwable e)
        {
            RecurrentComplex.logger.error("Error loading special TileEntity", e);
        }

        return TileEntity.createAndLoadEntity(compound);
    }
}
