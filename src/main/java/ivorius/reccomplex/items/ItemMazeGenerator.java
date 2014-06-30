/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.items;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * Created by lukas on 06.06.14.
 */
public class ItemMazeGenerator extends ItemBlock
{
    public ItemMazeGenerator(Block block)
    {
        super(block);
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4)
    {
        super.addInformation(itemStack, par2EntityPlayer, par3List, par4);

//        if (itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("mazeGeneratorInfo"))
//        {
//            NBTTagCompound compound = itemStack.getTagCompound().getCompoundTag("mazeGeneratorInfo");
//        }
    }
}
