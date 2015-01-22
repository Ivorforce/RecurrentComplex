/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex;

import cpw.mods.fml.common.event.FMLInterModComms;
import ivorius.ivtoolkit.tools.IvFMLIntercommHandler;
import ivorius.ivtoolkit.tools.IvNBTHelper;
import ivorius.reccomplex.dimensions.DimensionDictionary;
import ivorius.reccomplex.worldgen.StructureRegistry;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollectionRegistry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

/**
 * Created by lukas on 07.06.14.
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
        if (isMessage("loadStructure", message, NBTTagCompound.class))
        {
            // Note that this is not required for default loading, using the correct directories.
            // Only use this if you want to load it conditionally.

            NBTTagCompound cmp = message.getNBTValue();
            String structurePath = cmp.getString("structurePath");
            String structureID = cmp.getString("structureID");
            boolean generates = cmp.getBoolean("generates");

            if (!StructureRegistry.registerStructure(new ResourceLocation(structurePath), structureID, generates))
                getLogger().warn(String.format("Could not find structure with path '%s and id '%s'", structurePath, structureID));

            return true;
        }
        else if (isMessage("loadInventoryGenerator", message, NBTTagCompound.class))
        {
            // Note that this is not required for default loading, using the correct directories.
            // Only use this if you want to load it conditionally.

            NBTTagCompound cmp = message.getNBTValue();
            String genPath = cmp.getString("genPath");
            String genID = cmp.getString("genID");

            if (!GenericItemCollectionRegistry.register(new ResourceLocation(genPath), genID))
                getLogger().warn(String.format("Could not find inventory generator with path '%s and id '%s'", genPath, genID));

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
        else if (isMessage("registerDimensionSubtypes", message, String.class))
        {
            NBTTagCompound cmp = message.getNBTValue();
            String type = cmp.getString("type");
            String[] subtypes = IvNBTHelper.readNBTStrings("subtypes", cmp); // NBTTagList of NBTTagString

            if (type != null)
                DimensionDictionary.registerSubtypes(type, Arrays.asList(subtypes));
            else
                getLogger().warn("Could not handle message with key '" + message.key + "' - missing 'subtypes' key!");

            return true;
        }
        else if (isMessage("registerDimensionSupertypes", message, String.class))
        {
            NBTTagCompound cmp = message.getNBTValue();
            String type = cmp.getString("type");
            String[] subtypes = IvNBTHelper.readNBTStrings("supertypes", cmp); // NBTTagList of NBTTagString

            if (type != null)
                DimensionDictionary.registerSupertypes(type, Arrays.asList(subtypes));
            else
                getLogger().warn("Could not handle message with key '" + message.key + "' - missing 'supertypes' key!");

            return true;
        }

        return false;
    }
}
