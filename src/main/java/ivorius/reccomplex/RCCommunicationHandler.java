/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex;

import cpw.mods.fml.common.event.FMLInterModComms;
import ivorius.ivtoolkit.tools.IvFMLIntercommHandler;
import ivorius.ivtoolkit.tools.IvNBTHelper;
import ivorius.reccomplex.dimensions.DimensionDictionary;
import ivorius.reccomplex.files.FileLoadContext;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.generic.matchers.DimensionMatcher;
import ivorius.reccomplex.worldgen.StructureSelector;
import ivorius.reccomplex.structures.generic.matchers.BiomeMatcher;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollectionRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.helpers.Strings;

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
            RecurrentComplex.fileTypeRegistry.tryLoad(resourceLocation, new FileLoadContext(resourceLocation.getResourceDomain(), generates, false, genID));

            return true;
        }
        else if (isMessage("loadStructure", message, NBTTagCompound.class))
        {
            // Note that this is not required for default loading, using the correct directories.
            // Only use this if you want to load it conditionally.

            NBTTagCompound cmp = message.getNBTValue();
            String structurePath = cmp.getString("structurePath");
            String structureID = cmp.getString("structureID");
            boolean generates = cmp.getBoolean("generates");

            ResourceLocation resourceLocation = new ResourceLocation(structurePath);
            RecurrentComplex.fileTypeRegistry.tryLoad(resourceLocation, new FileLoadContext(resourceLocation.getResourceDomain(), generates, false, structureID));

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

            if (!Strings.isEmpty(type))
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

            if (!Strings.isEmpty(type))
                DimensionDictionary.registerSupertypes(type, Arrays.asList(subtypes));
            else
                getLogger().warn("Could not handle message with key '" + message.key + "' - missing 'supertypes' key!");

            return true;
        }
        else if (isMessage("registerSimpleSpawnCategory", message, NBTTagCompound.class))
        {
            // Legacy. Use natural spawn category files (rcnc) instead.

            NBTTagCompound cmp = message.getNBTValue();
            String id = cmp.getString("id");

            // If no biome selector matches, this value will be returned.
            float defaultSpawnChance = cmp.getFloat("defaultSpawnChance");
            boolean selectableInGui = cmp.getBoolean("selectableInGui");

            // If less structures than this cap are registered, the overall spawn chance will decrease so not to spam the same structures over and over.
            int structureMinCap = cmp.getInteger("structureMinCap");

            // List of {chance}:{ID}. These selectors work the same as structure biome selectors.
            // e.g. 0.232:Type:PLAINS,COLD
            // e.g. 1:Ocean
            String[] biomeTypes = IvNBTHelper.readNBTStrings("biomeTypes", cmp); // NBTTagList of NBTTagString

            if (!Strings.isEmpty(id))
            {
                StructureSelector.GenerationInfo[] biomeInfos = new StructureSelector.GenerationInfo[biomeTypes.length];
                for (int i = 0; i < biomeTypes.length; i++)
                {
                    String[] biomeParts = biomeTypes[i].split(":", 2);
                    biomeInfos[i] = new StructureSelector.GenerationInfo(Float.valueOf(biomeParts[0]), new BiomeMatcher(biomeParts[1]), new DimensionMatcher(""));
                }

                StructureSelector.registerCategory(id, new StructureSelector.SimpleCategory(defaultSpawnChance,
                        Arrays.asList(biomeInfos), selectableInGui, structureMinCap), false);
            }
            else
                getLogger().warn("Could not handle message with key '" + message.key + "' - missing 'id' key!");

            return true;
        }
        else if (isMessage("registerLegacyBlockIds", message, NBTTagCompound.class))
        {
            NBTTagCompound cmp = message.getNBTValue();

            Block block = RecurrentComplex.mcregistry.blockFromID(cmp.getString("block"));

            if (block != null)
            {
                boolean inferItem = cmp.getBoolean("inferItem");
                String[] legacyIDs = IvNBTHelper.readNBTStrings("legacyIDs", cmp); // NBTTagList of NBTTagString

                RecurrentComplex.remapper.registerLegacyIDs(block, inferItem, legacyIDs);
            }
            else
                getLogger().warn("Could not handle message with key '" + message.key + "' - could not find block!");
        }
        else if (isMessage("registerLegacyItemIds", message, NBTTagCompound.class))
        {
            NBTTagCompound cmp = message.getNBTValue();

            Item item = RecurrentComplex.mcregistry.itemFromID(cmp.getString("item"));

            if (item != null)
            {
                String[] legacyIDs = IvNBTHelper.readNBTStrings("legacyIDs", cmp); // NBTTagList of NBTTagString

                RecurrentComplex.remapper.registerLegacyIDs(item, legacyIDs);
            }
            else
                getLogger().warn("Could not handle message with key '" + message.key + "' - could not find item!");
        }

        return false;
    }
}
