/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.inventory;

import ivorius.reccomplex.files.SimpleCustomizableRegistry;

import java.util.*;

/**
 * Created by lukas on 25.05.14.
 */
public class WeightedItemCollectionRegistry extends SimpleCustomizableRegistry<WeightedItemCollection>
{
    public static WeightedItemCollectionRegistry INSTANCE = new WeightedItemCollectionRegistry();

    public WeightedItemCollectionRegistry()
    {
        super("weighted item collection");
    }
}
