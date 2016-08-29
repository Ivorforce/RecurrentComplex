/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.blocks;

import ivorius.reccomplex.scripts.world.WorldScript;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Created by lukas on 06.06.14.
 */
public class BlockSpawnCommand extends Block
{
    public BlockSpawnCommand()
    {
        super(Material.IRON);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (!worldIn.isRemote && playerIn instanceof EntityPlayerMP && playerIn.canCommandSenderUseCommand(2, ""))
        {
            TileEntity tileEntity = worldIn.getTileEntity(pos);

            WorldScript script = ((TileEntitySpawnCommand) tileEntity).script;
            worldIn.setBlockState(pos, RCBlocks.spawnScript.getDefaultState());
            ((TileEntitySpawnScript) worldIn.getTileEntity(pos)).script.scripts.add(script);
        }

        return true;
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return new TileEntitySpawnCommand();
    }
}
