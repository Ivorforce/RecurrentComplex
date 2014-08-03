/*
 * Copyright (c) 2014, Lukas Tenbrink.
 * http://lukas.axxim.net
 *
 * You are free to:
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes, unless you have a permit by the creator.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package ivorius.ivtoolkit.blocks;


import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class IvBlockMultiblock extends BlockContainer
{
    protected IvBlockMultiblock(Material par2Material)
    {
        super(par2Material);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int blockMeta)
    {
        TileEntity tileEntity = world.getTileEntity(x, y, z);

        if (tileEntity instanceof IvTileEntityMultiBlock)
        {
            IvTileEntityMultiBlock tileEntityMultiBlock = (IvTileEntityMultiBlock) tileEntity;

            if (!tileEntityMultiBlock.multiblockInvalid)
            {
                tileEntityMultiBlock.multiblockInvalid = true;

                if (tileEntityMultiBlock.isParent())
                {
                    if (!world.isRemote)
                    {
                        this.parentBlockDropItemContents(world, tileEntityMultiBlock, x, y, z, block, blockMeta);
                    }

                    int[][] toDestroy = tileEntityMultiBlock.getActiveChildCoords();

                    TileEntity[] destroyTEs = new TileEntity[toDestroy.length];

                    for (int i = 0; i < toDestroy.length; i++)
                    {
                        int[] coords = toDestroy[i];
                        TileEntity otherTE = world.getTileEntity(coords[0], coords[1], coords[2]);

                        if (otherTE instanceof IvTileEntityMultiBlock && !((IvTileEntityMultiBlock) otherTE).multiblockInvalid)
                        {
                            destroyTEs[i] = otherTE;
                            ((IvTileEntityMultiBlock) otherTE).multiblockInvalid = true;
                        }
                    }

                    for (TileEntity destroyTE : destroyTEs)
                    {
                        if (destroyTE != null)
                        {
                            world.playAuxSFX(2001, destroyTE.xCoord, destroyTE.yCoord, destroyTE.zCoord, Block.getIdFromBlock(block) + (world.getBlockMetadata(destroyTE.xCoord, destroyTE.yCoord, destroyTE.zCoord) << 12));

//                            if (world.isRemote)
//                            {
//                                Minecraft.getMinecraft().effectRenderer.addBlockDestroyEffects(destroyTE.xCoord, destroyTE.yCoord, destroyTE.zCoord, destroyTE.getBlockType(), destroyTE.getBlockMetadata());
//                            }

                            world.setBlockToAir(destroyTE.xCoord, destroyTE.yCoord, destroyTE.zCoord);
                        }
                    }
                }
                else
                {
                    int[] parentTECoords = tileEntityMultiBlock.getActiveParentCoords();
                    TileEntity parentTE = world.getTileEntity(parentTECoords[0], parentTECoords[1], parentTECoords[2]);
                    if (parentTE instanceof IvTileEntityMultiBlock && !((IvTileEntityMultiBlock) parentTE).multiblockInvalid)
                    {
                        world.playAuxSFX(2001, parentTE.xCoord, parentTE.yCoord, parentTE.zCoord, Block.getIdFromBlock(block) + (world.getBlockMetadata(parentTE.xCoord, parentTE.yCoord, parentTE.zCoord) << 12));

//                        if (world.isRemote)
//                        {
//                            Minecraft.getMinecraft().effectRenderer.addBlockDestroyEffects(parentTE.xCoord, parentTE.yCoord, parentTE.zCoord, parentTE.getBlockType(), parentTE.getBlockMetadata());
//                        }

                        world.setBlockToAir(parentTE.xCoord, parentTE.yCoord, parentTE.zCoord);
                    }
                }
            }
        }

        super.breakBlock(world, x, y, z, block, blockMeta);
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest)
    {
        IvTileEntityMultiBlock tileEntityMultiBlock = getValidatedTotalParent(this, world, x, y, z);

        if (tileEntityMultiBlock != null)
        {
            int blockMeta = world.getBlockMetadata(x, y, z);

            if (!world.isRemote && willHarvest)
            {
                this.parentBlockHarvestItem(world, tileEntityMultiBlock, x, y, z, this, blockMeta);
            }
        }

        return super.removedByPlayer(world, player, x, y, z, willHarvest);
    }

    @Override
    public void onBlockExploded(World world, int x, int y, int z, Explosion explosion)
    {
        IvTileEntityMultiBlock tileEntityMultiBlock = getValidatedTotalParent(this, world, x, y, z);

        if (tileEntityMultiBlock != null)
        {
            int blockMeta = world.getBlockMetadata(x, y, z);

            if (!world.isRemote)
            {
                this.parentBlockHarvestItem(world, tileEntityMultiBlock, x, y, z, this, blockMeta);
            }
        }

        super.onBlockExploded(world, x, y, z, explosion);
    }

    public void parentBlockDropItemContents(World world, IvTileEntityMultiBlock tileEntity, int x, int y, int z, Block block, int metadata)
    {
    }

    public void parentBlockHarvestItem(World world, IvTileEntityMultiBlock tileEntity, int x, int y, int z, Block block, int metadata)
    {
    }

    @Override
    public void dropBlockAsItemWithChance(World par1World, int par2, int par3, int par4, int par5, float par6, int par7)
    {

    }

//    @Override
//    public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, Block par5)
//    {
//        super.onNeighborBlockChange(par1World, par2, par3, par4, par5);
//
//        validateMultiblock(this, par1World, par2, par3, par4);
//    }

    public static boolean validateMultiblock(Block block, IBlockAccess world, int x, int y, int z)
    {
        if (world.getBlock(x, y, z) != block)
        {
            return false;
        }

        boolean isValidChild = false;
        boolean destroy = false;

        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof IvTileEntityMultiBlock)
        {
            IvTileEntityMultiBlock tileEntityMultiBlock = (IvTileEntityMultiBlock) tileEntity;
            IvTileEntityMultiBlock parent = tileEntityMultiBlock.getParent();

            isValidChild = tileEntityMultiBlock.isParent() || (parent != null && parent.isParent());
            destroy = !isValidChild && !tileEntityMultiBlock.multiblockInvalid;
        }

        if (destroy)
        {
            if (world instanceof World)
                ((World) world).setBlockToAir(x, y, z);
        }

        return isValidChild;
    }

    public static IvTileEntityMultiBlock getValidatedIfParent(Block block, World world, int x, int y, int z)
    {
        if (validateMultiblock(block, world, x, y, z))
        {
            IvTileEntityMultiBlock tileEntity = (IvTileEntityMultiBlock) world.getTileEntity(x, y, z);

            return tileEntity.isParent() ? tileEntity : null;
        }

        return null;
    }

    public static IvTileEntityMultiBlock getValidatedTotalParent(Block block, IBlockAccess world, int x, int y, int z)
    {
        if (validateMultiblock(block, world, x, y, z))
        {
            TileEntity tileEntity = world.getTileEntity(x, y, z);

            if (tileEntity instanceof IvTileEntityMultiBlock)
            {
                return ((IvTileEntityMultiBlock) tileEntity).getTotalParent();
            }
        }

        return null;
    }
}
