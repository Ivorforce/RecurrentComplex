/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.blocks;

import ivorius.reccomplex.scripts.world.WorldScript;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Created by lukas on 06.06.14.
 */
public class BlockMazeGenerator extends Block
{
    public BlockMazeGenerator()
    {
        super(Material.iron);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
    {
        if (!world.isRemote && player instanceof EntityPlayerMP && player.canCommandSenderUseCommand(2, ""))
        {
            TileEntity tileEntity = world.getTileEntity(x, y, z);

            WorldScript script = ((TileEntityMazeGenerator) tileEntity).script;
            world.setBlock(x, y, z, RCBlocks.spawnScript);
            ((TileEntitySpawnScript) world.getTileEntity(x, y, z)).script.scripts.add(script);

        }

        return true;
    }

    @Override
    public boolean hasTileEntity(int metadata)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World var1, int var2)
    {
        return new TileEntityMazeGenerator();
    }
}
