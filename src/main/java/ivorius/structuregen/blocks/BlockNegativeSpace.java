/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.blocks;

import ivorius.structuregen.StructureGen;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class BlockNegativeSpace extends Block
{
    private IIcon[] icons = new IIcon[2];

    public BlockNegativeSpace()
    {
        super(StructureGen.materialNegativeSpace);

        float lowB = 1.0f / 16.0f * 5.5f;
        float highB = 1.0f / 16.0f * 9.5f;
        setBlockBounds(lowB, lowB, lowB, highB, highB, highB);
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean isNormalCube()
    {
        return false;
    }

    @Override
    public void dropBlockAsItemWithChance(World p_149690_1_, int p_149690_2_, int p_149690_3_, int p_149690_4_, int p_149690_5_, float p_149690_6_, int p_149690_7_)
    {

    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
    {
        return null;
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list)
    {
        for (int i = 0; i < 2; i++)
        {
            list.add(new ItemStack(item, 1, i));
        }
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister)
    {
        for (int i = 0; i < 2; i++)
        {
            icons[i] = iconRegister.registerIcon(getTextureName() + "." + i);
        }
    }

    @Override
    public IIcon getIcon(int side, int meta)
    {
        return meta < icons.length ? icons[meta] : icons[0];
    }
}
