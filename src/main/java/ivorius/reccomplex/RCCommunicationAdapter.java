/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.event.FMLInterModComms;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * @ Mod Authors
 * Feel free to copy this class to your code to be able to use @{@link RCCommunicationHandler}'s functionality easily.
 */
public class RCCommunicationAdapter
{
    public static String MOD_ID = "reccomplex";

    public static void loadFile(ResourceLocation path, @Nullable String asID, boolean active)
    {
        NBTTagCompound compound = new NBTTagCompound();

        compound.setString("genPath", path.toString());
        if (asID != null) compound.setString("genID", asID);
        compound.setBoolean("generates", active);

        FMLInterModComms.sendMessage(MOD_ID, "loadFile", compound);
    }

    public static void registerDimension(int dimensionID, Collection<String> types)
    {
        NBTTagCompound compound = new NBTTagCompound();

        compound.setInteger("dimensionID", dimensionID);
        writeNBTStrings("types", types, compound);

        FMLInterModComms.sendMessage(MOD_ID, "registerDimension", compound);
    }

    public static void unregisterDimension(int dimensionID, Collection<String> types)
    {
        NBTTagCompound compound = new NBTTagCompound();

        compound.setInteger("dimensionID", dimensionID);
        writeNBTStrings("types", types, compound);

        FMLInterModComms.sendMessage(MOD_ID, "unregisterDimension", compound);
    }

    public static void registerDimensionType(String type)
    {
        FMLInterModComms.sendMessage(MOD_ID, "registerDimensionType", type);
    }

    public static void registerDimensionSubtypes(String type, Collection<String> subtypes)
    {
        NBTTagCompound compound = new NBTTagCompound();

        compound.setString("type", type);
        writeNBTStrings("subtypes", subtypes, compound);

        FMLInterModComms.sendMessage(MOD_ID, "registerDimensionSubtypes", compound);
    }

    public static void registerDimensionSupertypes(String type, Collection<String> supertypes)
    {
        NBTTagCompound compound = new NBTTagCompound();

        compound.setString("type", type);
        writeNBTStrings("supertypes", supertypes, compound);

        FMLInterModComms.sendMessage(MOD_ID, "registerDimensionSupertypes", compound);
    }

    public static void registerLegacyBlockIDs(Block block, Collection<String> legacyIDs, boolean inferItem)
    {
        NBTTagCompound compound = new NBTTagCompound();

        compound.setString("block", Block.REGISTRY.getNameForObject(block).toString());
        writeNBTStrings("legacyIDs", legacyIDs, compound);
        compound.setBoolean("inferItem", inferItem);

        FMLInterModComms.sendMessage(MOD_ID, "registerLegacyBlockIDs", compound);
    }

    public static void registerLegacyItemIds(Item item, Collection<String> legacyIDs)
    {
        NBTTagCompound compound = new NBTTagCompound();

        compound.setString("item", Item.REGISTRY.getNameForObject(item).toString());
        writeNBTStrings("legacyIDs", legacyIDs, compound);

        FMLInterModComms.sendMessage(MOD_ID, "registerLegacyItemIds", compound);
    }

    public static void writeNBTStrings(String id, Collection<String> strings, NBTTagCompound compound)
    {
        if (strings != null)
        {
            NBTTagList nbtTagList = new NBTTagList();

            for (String s : strings)
                nbtTagList.appendTag(new NBTTagString(s));

            compound.setTag(id, nbtTagList);
        }
    }
}
