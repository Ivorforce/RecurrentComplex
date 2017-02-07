/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.storage.loot;

import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public interface WeightedItemCollection
{
    @Nullable
    ItemStack getRandomItemStack(WorldServer server, Random random);

    String getDescriptor();
}
