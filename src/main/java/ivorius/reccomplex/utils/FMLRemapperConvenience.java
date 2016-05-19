/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import ivorius.ivtoolkit.tools.MCRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import java.util.stream.Stream;

/**
 * Created by lukas on 25.04.16.
 */
public class FMLRemapperConvenience
{
    protected String domain;

    protected MCRegistry parent;
    protected FMLRemapper remapper;

    public FMLRemapperConvenience(String domain, MCRegistry parent, FMLRemapper remapper)
    {
        this.domain = domain;
        this.parent = parent;
        this.remapper = remapper;
    }

    //    public void registerLegacyIDs(Class<? extends TileEntity> tileClass, String... oldIDs)
//    {
//        registerLegacyTileEntityIDs(TileEntity.createAndLoadEntity());
//    }

    public void registerLegacyIDs(Block block, boolean inferItem, String... oldIDs)
    {
        remapper.registerLegacyBlockIDs(parent.idFromBlock(block), inferItem, Stream.of(oldIDs).map(id -> new ResourceLocation(domain, id)).toArray(ResourceLocation[]::new));
    }

    public void registerLegacyIDs(Item item, String... oldIDs)
    {
        remapper.registerLegacyItemIDs(parent.idFromItem(item), Stream.of(oldIDs).map(id -> new ResourceLocation(domain, id)).toArray(ResourceLocation[]::new));
    }
}
