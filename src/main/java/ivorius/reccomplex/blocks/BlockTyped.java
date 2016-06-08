package ivorius.reccomplex.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class BlockTyped extends Block
{
    public static final PropertyInteger TYPE = PropertyInteger.create("type", 0, 15);

    public BlockTyped(Material materialIn)
    {
        super(materialIn);
        this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, 0));
        this.setCreativeTab(CreativeTabs.tabBlock);
    }

    public int damageDropped(IBlockState state)
    {
        return state.getValue(TYPE);
    }

    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List list)
    {
        for (int j = 0; j < 16; ++j)
            list.add(new ItemStack(itemIn, 1, j));
    }

    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(TYPE, meta);
    }

    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(TYPE);
    }

    protected BlockState createBlockState()
    {
        return new BlockState(this, TYPE);
    }
}