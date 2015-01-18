/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.items;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlockWithMetadata;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * Created by lukas on 25.05.14.
 */
public class ItemBlockGenericSolid extends ItemBlockWithMetadata
{
    public ItemBlockGenericSolid(Block block)
    {
        super(block, block);
    }

    @Override
    public String getUnlocalizedName(ItemStack par1ItemStack)
    {
        return super.getUnlocalizedName(par1ItemStack) + ".meta." + par1ItemStack.getItemDamage();
    }
}
