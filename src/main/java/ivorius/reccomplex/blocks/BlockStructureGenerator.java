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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/**
 * Created by lukas on 06.06.14.
 */
public class BlockStructureGenerator extends Block
{
    public BlockStructureGenerator()
    {
        super(Material.iron);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (!worldIn.isRemote && playerIn instanceof EntityPlayerMP && playerIn.canCommandSenderUseCommand(2, ""))
        {
            TileEntity tileEntity = worldIn.getTileEntity(pos);

            WorldScript script = ((TileEntityStructureGenerator) tileEntity).script;
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

    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return new TileEntityStructureGenerator();
    }
}
