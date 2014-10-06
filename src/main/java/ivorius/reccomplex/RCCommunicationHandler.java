/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex;

import cpw.mods.fml.common.event.FMLInterModComms;
import ivorius.ivtoolkit.tools.IvFMLIntercommHandler;
import ivorius.reccomplex.worldgen.StructureHandler;
import ivorius.reccomplex.worldgen.inventory.InventoryGenerationHandler;
import org.apache.logging.log4j.Logger;

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
        return false;
    }
}
