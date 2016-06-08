/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
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
                    ResourceLocation remap = remapper.remapBlock(new ResourceLocation(missingMapping.name));
                    if (remap != null)
                        missingMapping.remap(parent.blockFromID(remap));
                    break;
                }
                case ITEM:
                {
                    ResourceLocation remap = remapper.remapItem(new ResourceLocation(missingMapping.name));
                    if (remap != null)
                        missingMapping.remap(parent.itemFromID(remap));
                    break;
                }
            }
        }
    }
}
