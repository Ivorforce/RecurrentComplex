/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import ivorius.ivtoolkit.tools.MCRegistry;
import net.minecraftforge.event.RegistryEvent;

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

    public void onMissingMapping(RegistryEvent.MissingMappings<?> event)
    {
        for (RegistryEvent.MissingMappings.Mapping missingMapping : event.getAllMappings())
        {
            if (missingMapping.getTarget() instanceof Block)
            {
                ResourceLocation remap = remapper.remapBlock(missingMapping.key);
                if (remap != null)
                    //noinspection unchecked
                    missingMapping.remap(parent.blockFromID(remap));
                break;
            }
            else if (missingMapping.getTarget() instanceof Item)
            {
                ResourceLocation remap = remapper.remapItem(missingMapping.key);
                if (remap != null)
                    //noinspection unchecked
                    missingMapping.remap(parent.itemFromID(remap));
                break;
            }
        }
    }
}
