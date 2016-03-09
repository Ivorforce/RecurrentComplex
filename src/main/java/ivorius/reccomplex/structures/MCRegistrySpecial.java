/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.RecurrentComplex;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lukas on 30.06.14.
 */
public class MCRegistrySpecial implements MCRegistry
{
    public static final String HIDDEN_ITEM_TAG = "RC_HIDDEN_ITEM";

    public static final MCRegistrySpecial INSTANCE = new MCRegistrySpecial();

    private final BiMap<String, Item> itemMap = HashBiMap.create();
    private final BiMap<String, Block> blockMap = HashBiMap.create();
    private final BiMap<String, Item> itemMapFallback = HashBiMap.create();
    private final BiMap<String, Block> blockMapFallback = HashBiMap.create();
    private final Map<String, Class<? extends TileEntity>> tileEntityMap = new HashMap<>();

    private ItemHidingRegistry itemHidingRegistry = new ItemHidingRegistry(this);

    public void register(String id, Item item)
    {
        itemMap.put(id, item);
    }

    public void register(String id, Block block)
    {
        blockMap.put(id, block);
    }

    public void registerFallback(String id, Item item)
    {
        itemMapFallback.put(id, item);
    }

    public void registerFallback(String id, Block block)
    {
        blockMapFallback.put(id, block);
    }

    public void register(String id, Class<? extends TileEntity> tileEntity)
    {
        tileEntityMap.put(id, tileEntity);
    }

    public ItemHidingRegistry itemHidingMode()
    {
        return itemHidingRegistry;
    }

    @Override
    public Item itemFromID(String itemID)
    {
        Item item = itemMap.get(itemID);
        if (item == null) item = itemMapFallback.get(itemID);
        return item != null ? item : (Item) Item.itemRegistry.getObject(itemID);
    }

    @Override
    public String idFromItem(Item item)
    {
        String id = itemMap.inverse().get(item);
        return id != null ? id : Item.itemRegistry.getNameForObject(item);
    }

    @Override
    public void modifyItemStackCompound(NBTTagCompound compound, String itemID)
    {

    }

    public boolean isSafe(Item item)
    {
        return itemMap.isEmpty() || !itemMap.containsValue(item);
    }

    @Override
    public Block blockFromID(String blockID)
    {
        Block block = blockMap.get(blockID);
        if (block == null) block = blockMapFallback.get(blockID);
        return block != null ? block : Block.getBlockFromName(blockID);
    }

    @Override
    public String idFromBlock(Block block)
    {
        String id = blockMap.inverse().get(block);
        return id != null ? id : Block.blockRegistry.getNameForObject(block);
    }

    public boolean isSafe(Block block)
    {
        return blockMap.isEmpty() || !blockMap.containsValue(block);
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

    public boolean isSafe(TileEntity tileEntity)
    {
        return tileEntityMap.isEmpty() || !tileEntityMap.containsValue(tileEntity.getClass());
    }

    public static class ItemHidingRegistry implements MCRegistry
    {
        protected MCRegistrySpecial parent;

        public ItemHidingRegistry(MCRegistrySpecial parent)
        {
            this.parent = parent;
        }

        @Override
        public Item itemFromID(String itemID)
        {
            Item item = parent.itemMap.get(itemID);
            if (item == null) item = parent.itemMapFallback.get(itemID);
            return item != null ? Items.coal : (Item) Item.itemRegistry.getObject(itemID);
        }

        @Override
        public String idFromItem(Item item)
        {
            return parent.idFromItem(item);
        }

        public String containedItemID(ItemStack stack)
        {
            return parent.idFromItem(containedItem(stack));
        }

        public Item containedItem(ItemStack stack)
        {
            Item hidden = hiddenItem(stack);
            return hidden != null ? hidden : stack.getItem();
        }

        @Nullable
        public Item hiddenItem(ItemStack stack)
        {
            return stack.hasTagCompound() && stack.getTagCompound().hasKey(HIDDEN_ITEM_TAG, Constants.NBT.TAG_STRING)
                    ? parent.itemFromID(stack.getTagCompound().getString(HIDDEN_ITEM_TAG))
                    : null;
        }

        public ItemStack constructItemStack(String itemID, int stackSize, int metadata)
        {
            return constructItemStack(parent.itemFromID(itemID), stackSize, metadata);
        }

        public ItemStack constructItemStack(Item item, int stackSize, int metadata)
        {
            String hiddenID = parent.itemMap.inverse().get(item);
            if (hiddenID != null)
            {
                ItemStack stack = new ItemStack(Items.coal, stackSize, metadata);
                stack.setTagInfo(HIDDEN_ITEM_TAG, new NBTTagString(hiddenID));
                return stack;
            }
            else
                return new ItemStack(item, stackSize, metadata);
        }

        @Override
        public void modifyItemStackCompound(NBTTagCompound compound, String itemID)
        {
            Item item = parent.itemMap.get(itemID);
            if (item == null) {
                item = parent.itemMapFallback.get(itemID);
                if (item != null) {
                    String _itemID = parent.idFromItem(item);
                    if (_itemID != null) itemID = _itemID;
                }
            }
            if (item != null)
            {
                NBTTagCompound stackNBT;
                if (compound.hasKey("tag", Constants.NBT.TAG_COMPOUND))
                    stackNBT = compound.getCompoundTag("tag");
                else
                    compound.setTag("tag", stackNBT = new NBTTagCompound());

                stackNBT.setString(HIDDEN_ITEM_TAG, itemID);
            }
        }

        @Override
        public Block blockFromID(String blockID)
        {
            return parent.blockFromID(blockID);
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
}
