/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.block;

import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.nbt.NBTStorable;
import ivorius.reccomplex.network.PacketEditTileEntity;
import ivorius.reccomplex.utils.UnstableBlock;
import ivorius.reccomplex.world.gen.feature.StructureGenerator;
import ivorius.reccomplex.world.gen.feature.structure.generic.transformers.RunTransformer;
import ivorius.reccomplex.world.gen.feature.structure.generic.transformers.TransformerMulti;
import ivorius.reccomplex.world.gen.script.WorldScriptMulti;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

/**
 * Created by lukas on 06.06.14.
 */
public class BlockScript extends Block implements UnstableBlock
{
    public BlockScript()
    {
        super(Material.IRON);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (!worldIn.isRemote && playerIn instanceof EntityPlayerMP && playerIn.canUseCommand(2, ""))
        {
            TileEntity tileEntity = worldIn.getTileEntity(pos);

            RecurrentComplex.network.sendTo(new PacketEditTileEntity((TileEntityBlockScript) tileEntity), (EntityPlayerMP) playerIn);
        }

        return true;
    }

    @Override
    public int tickRate(World worldIn)
    {
        return 4;
    }

    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (!worldIn.isRemote)
        {
            TileEntityBlockScript tileEntity = (TileEntityBlockScript) worldIn.getTileEntity(pos);
            StructureGenerator<NBTStorable> generator = new StructureGenerator<>().world((WorldServer) worldIn)
                    .boundingBox(new StructureBoundingBox(pos, pos)).transform(AxisAlignedTransform2D.ORIGINAL);

            WorldScriptMulti.InstanceData instanceData = tileEntity.doPrepareInstanceData(generator.prepare().get());
            if (instanceData != null)
                tileEntity.generate(generator.spawn().get(), new RunTransformer(new TransformerMulti(), new TransformerMulti.InstanceData()), instanceData);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        boolean flag = worldIn.isBlockPowered(pos) || worldIn.isBlockPowered(pos.up());
        TileEntityBlockScript tileEntity = (TileEntityBlockScript) worldIn.getTileEntity(pos);

        if (!tileEntity.redstoneTriggerable) {
            return;
        }

        boolean flag1 = tileEntity.redstoneTriggered;

        if (flag && !flag1)
        {
            worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
            tileEntity.redstoneTriggered = true;
        }
        else if (!flag && flag1)
        {
            tileEntity.redstoneTriggered = false;
        }
    }

    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
    {
        TileEntity tileEntity = world.getTileEntity(pos);
        NBTTagCompound compound = new NBTTagCompound();
        ((TileEntityBlockScript) tileEntity).writeSyncedNBT(compound);

        ItemStack returnStack = new ItemStack(Item.getItemFromBlock(this));
        returnStack.setTagInfo("scriptInfo", compound);

        return returnStack;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("scriptInfo", Constants.NBT.TAG_COMPOUND))
        {
            NBTTagCompound compound = stack.getTagCompound().getCompoundTag("scriptInfo");
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            ((TileEntityBlockScript) tileEntity).readSyncedNBT(compound);
        }
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
        return new TileEntityBlockScript();
    }
}
