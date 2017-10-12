/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex;

import com.google.common.base.Strings;
import ivorius.reccomplex.files.loading.LeveledRegistry;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import ivorius.ivtoolkit.tools.IvFMLIntercommHandler;
import ivorius.ivtoolkit.tools.IvNBTHelper;
import ivorius.reccomplex.dimensions.DimensionDictionary;
import ivorius.reccomplex.files.loading.FileLoadContext;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

/**
 * @ Mod Authors
 * This is the FML Intercomm Message handler for Recurrent Complex.
 * @see RCCommunicationAdapter for a copyable sample implementation.
 */
public class RCCommunicationHandler extends IvFMLIntercommHandler
{
    public RCCommunicationHandler(Logger logger, String modOwnerID, Object modInstance)
    {
        super(logger, modOwnerID, modInstance);
    }

    @Override
    protected boolean handleMessage(FMLInterModComms.IMCMessage message, boolean server, boolean runtime)
    {
        if (isMessage("loadFile", message, NBTTagCompound.class)
                || isMessage("loadInventoryGenerator", message, NBTTagCompound.class))
        {
            // Note that this is not required for default loading, using the correct directories.
            // Only use this if you want to load it conditionally.
            NBTTagCompound cmp = message.getNBTValue();

            String genPath = cmp.getString("genPath");
            String genID = cmp.hasKey("genID", Constants.NBT.TAG_STRING) ? cmp.getString("genID") : null;
            boolean generates = cmp.getBoolean("generates");

            ResourceLocation resourceLocation = new ResourceLocation(genPath);
            RecurrentComplex.loader.tryLoad(resourceLocation, genID, new FileLoadContext(resourceLocation.getResourceDomain(), generates, LeveledRegistry.Level.INTERNAL));

            return true;
        }
        else if (isMessage("registerDimension", message, NBTTagCompound.class))
        {
            NBTTagCompound cmp = message.getNBTValue();

            int dimensionID = cmp.getInteger("dimensionID");
            String[] types = IvNBTHelper.readNBTStrings("types", cmp); // NBTTagList of NBTTagString

            if (types != null)
                DimensionDictionary.registerDimensionTypes(dimensionID, Arrays.asList(types));
            else
                getLogger().warn("Could not handle message with key '" + message.key + "' - missing 'types' key!");

            return true;
        }
        else if (isMessage("unregisterDimension", message, NBTTagCompound.class))
        {
            NBTTagCompound cmp = message.getNBTValue();

            int dimensionID = cmp.getInteger("dimensionID");
            String[] types = IvNBTHelper.readNBTStrings("types", cmp); // NBTTagList of NBTTagString

            if (types != null)
                DimensionDictionary.unregisterDimensionTypes(dimensionID, Arrays.asList(types));
            else
                DimensionDictionary.unregisterDimensionTypes(dimensionID, null);

            return true;
        }
        else if (isMessage("registerDimensionType", message, String.class))
        {
            DimensionDictionary.registerType(message.getStringValue());
            return true;
        }
        else if (isMessage("registerDimensionSubtypes", message, NBTTagCompound.class))
        {
            NBTTagCompound cmp = message.getNBTValue();

            String type = cmp.getString("type");
            String[] subtypes = IvNBTHelper.readNBTStrings("subtypes", cmp); // NBTTagList of NBTTagString

            if (!Strings.isNullOrEmpty(type))
                DimensionDictionary.registerSubtypes(type, Arrays.asList(subtypes));
            else
                getLogger().warn("Could not handle message with key '" + message.key + "' - missing 'subtypes' key!");

            return true;
        }
        else if (isMessage("registerDimensionSupertypes", message, NBTTagCompound.class))
        {
            NBTTagCompound cmp = message.getNBTValue();
            String type = cmp.getString("type");
            String[] subtypes = IvNBTHelper.readNBTStrings("supertypes", cmp); // NBTTagList of NBTTagString

            if (!Strings.isNullOrEmpty(type))
                DimensionDictionary.registerSupertypes(type, Arrays.asList(subtypes));
            else
                getLogger().warn("Could not handle message with key '" + message.key + "' - missing 'supertypes' key!");

            return true;
        }
        else if (isMessage("registerLegacyBlockIds", message, NBTTagCompound.class))
        {
            NBTTagCompound cmp = message.getNBTValue();

            Block block = RecurrentComplex.specialRegistry.blockFromID(new ResourceLocation(cmp.getString("block")));

            if (block != null)
            {
                boolean inferItem = cmp.getBoolean("inferItem");
                String[] legacyIDs = IvNBTHelper.readNBTStrings("legacyIDs", cmp); // NBTTagList of NBTTagString

                RecurrentComplex.cremapper.registerLegacyIDs(block, inferItem, legacyIDs);
            }
            else
                getLogger().warn("Could not handle message with key '" + message.key + "' - could not find block!");
        }
        else if (isMessage("registerLegacyItemIds", message, NBTTagCompound.class))
        {
            NBTTagCompound cmp = message.getNBTValue();

            Item item = RecurrentComplex.specialRegistry.itemFromID(new ResourceLocation(cmp.getString("item")));

            if (item != null)
            {
                String[] legacyIDs = IvNBTHelper.readNBTStrings("legacyIDs", cmp); // NBTTagList of NBTTagString

                RecurrentComplex.cremapper.registerLegacyIDs(item, legacyIDs);
            }
            else
                getLogger().warn("Could not handle message with key '" + message.key + "' - could not find item!");
        }

        return false;
    }
}
