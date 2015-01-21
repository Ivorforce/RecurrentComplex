/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex;

import cpw.mods.fml.common.event.FMLInterModComms;
import ivorius.ivtoolkit.tools.IvFMLIntercommHandler;
import ivorius.ivtoolkit.tools.IvNBTHelper;
import ivorius.reccomplex.dimensions.DimensionDictionary;
import net.minecraft.nbt.NBTTagCompound;
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
        if (isMessage("registerDimension", message, NBTTagCompound.class))
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
