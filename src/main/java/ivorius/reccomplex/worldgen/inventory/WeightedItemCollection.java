/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;

import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public interface WeightedItemCollection
{
    ItemStack getRandomItemStack(WorldServer server, Random random);

    String getDescriptor();
}
