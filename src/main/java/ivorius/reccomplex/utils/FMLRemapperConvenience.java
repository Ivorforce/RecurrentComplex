/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import ivorius.ivtoolkit.tools.MCRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

/**
 * Created by lukas on 25.04.16.
 */
public class FMLRemapperConvenience
{
    protected MCRegistry parent;
    protected FMLRemapper remapper;

    public FMLRemapperConvenience(MCRegistry parent, FMLRemapper remapper)
    {
        this.parent = parent;
        this.remapper = remapper;
    }

    //    public void registerLegacyIDs(Class<? extends TileEntity> tileClass, String... oldIDs)
//    {
//        registerLegacyTileEntityIDs(TileEntity.createAndLoadEntity());
//    }

    public void registerLegacyIDs(Block block, boolean inferItem, String... oldIDs)
    {
        remapper.registerLegacyBlockIDs(parent.idFromBlock(block), inferItem, oldIDs);
    }

    public void registerLegacyIDs(Item item, String... oldIDs)
    {
        remapper.registerLegacyItemIDs(parent.idFromItem(item), oldIDs);
    }
}
