/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen;

import cpw.mods.fml.common.event.FMLInterModComms;
import ivorius.ivtoolkit.tools.IvFMLIntercommHandler;
import ivorius.structuregen.worldgen.StructureHandler;
import ivorius.structuregen.worldgen.inventory.InventoryGenerationHandler;
import org.apache.logging.log4j.Logger;

/**
 * Created by lukas on 07.06.14.
 */
public class SGCommunicationHandler extends IvFMLIntercommHandler
{
    public SGCommunicationHandler(Logger logger, String modOwnerID, Object modInstance)
    {
        super(logger, modOwnerID, modInstance);
    }

    @Override
    protected boolean handleMessage(FMLInterModComms.IMCMessage message, boolean server, boolean runtime)
    {
        if (isMessage("registerStructure", message, String.class))
        {
            StructureHandler.registerStructures(message.getSender(), true, message.getStringValue());

            return true;
        }
        else if (isMessage("registerSilentStructure", message, String.class))
        {
            StructureHandler.registerStructures(message.getSender(), false, message.getStringValue());

            return true;
        }
        else if (isMessage("registerInventoryGenerator", message, String.class))
        {
            InventoryGenerationHandler.registerInventoryHandlers(message.getSender(), message.getStringValue());

            return true;
        }

        return false;
    }
}
