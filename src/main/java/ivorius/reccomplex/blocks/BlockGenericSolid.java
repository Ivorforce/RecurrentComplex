/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.blocks;

import ivorius.reccomplex.blocks.materials.RCMaterials;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import java.util.List;

/**
 * Created by lukas on 06.06.14.
 */
public class BlockGenericSolid extends Block
{
    private IIcon[] icons = new IIcon[16];

    public BlockGenericSolid()
    {
        super(RCMaterials.materialGenericSolid);
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list)
    {
        for (int i = 0; i < 16; i++)
            list.add(new ItemStack(item, 1, i));
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister)
    {
        for (int i = 0; i < icons.length; i++)
            icons[i] = iconRegister.registerIcon(getTextureName() + "." + i);
    }

    @Override
    public IIcon getIcon(int side, int meta)
    {
        return meta < icons.length ? icons[meta] : icons[0];
    }

    @Override
    public int damageDropped(int meta)
    {
        return meta;
    }
}
