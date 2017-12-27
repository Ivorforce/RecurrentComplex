/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.storage.loot;

import ivorius.reccomplex.files.SimpleLeveledRegistry;

/**
 * Created by lukas on 25.05.14.
 */
public class WeightedItemCollectionRegistry extends SimpleLeveledRegistry<LootTable>
{
    public static WeightedItemCollectionRegistry INSTANCE = new WeightedItemCollectionRegistry();

    public WeightedItemCollectionRegistry()
    {
        super("weighted item collection");
    }
}
