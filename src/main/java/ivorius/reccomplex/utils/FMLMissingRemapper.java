/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ivorius.ivtoolkit.tools.MCRegistry;

/**
 * Created by lukas on 25.04.16.
 */
public class FMLMissingRemapper
{
    protected MCRegistry parent;
    protected FMLRemapper remapper;

    public FMLMissingRemapper(MCRegistry parent, FMLRemapper remapper)
    {
        this.parent = parent;
        this.remapper = remapper;
    }

    public void register()
    {

    }

    public void onMissingMapping(FMLMissingMappingsEvent event)
    {
        for (FMLMissingMappingsEvent.MissingMapping missingMapping : event.get())
        {
            switch (missingMapping.type)
            {
                case BLOCK:
                {
                    String remap = remapper.remapBlock(missingMapping.name);
                    if (remap != null)
                        missingMapping.remap(parent.blockFromID(remap));
                    break;
                }
                case ITEM:
                {
                    String remap = remapper.remapItem(missingMapping.name);
                    if (remap != null)
                        missingMapping.remap(parent.itemFromID(remap));
                    break;
                }
            }
        }
    }
}
