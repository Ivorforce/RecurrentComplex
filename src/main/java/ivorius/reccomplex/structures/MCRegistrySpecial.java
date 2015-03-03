/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures;

import ivorius.ivtoolkit.tools.MCRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

/**
 * Created by lukas on 30.06.14.
 */
public class MCRegistrySpecial implements MCRegistry
{
    public static final MCRegistrySpecial INSTANCE = new MCRegistrySpecial();

    @Override
    public Item itemFromID(String itemID)
    {
        return (Item) Item.itemRegistry.getObject(itemID);
    }

    @Override
    public Block blockFromID(String blockID)
    {
        return Block.getBlockFromName(blockID);
    }
}
