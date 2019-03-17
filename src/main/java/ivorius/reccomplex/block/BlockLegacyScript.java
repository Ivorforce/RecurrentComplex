/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.block;

import ivorius.reccomplex.nbt.NBTStorable;
import ivorius.reccomplex.utils.UnstableBlock;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureLoadContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructurePrepareContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureSpawnContext;
import ivorius.reccomplex.world.gen.feature.structure.generic.transformers.RunTransformer;
import ivorius.reccomplex.world.gen.script.WorldScript;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created by lukas on 07.10.16.
 */
public class BlockLegacyScript extends Block implements UnstableBlock
{
    public BlockLegacyScript()
    {
        super(Material.IRON);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (!worldIn.isRemote && playerIn instanceof EntityPlayerMP && playerIn.canUseCommand(2, ""))
        {
            TileEntity tileEntity = worldIn.getTileEntity(pos);

            if (tileEntity instanceof TileLegacyScript)
            {
                WorldScript script = ((TileLegacyScript<?, ?>) tileEntity).script;
                worldIn.setBlockState(pos, RCBlocks.spawnScript.getDefaultState());

                TileEntity newTileEntity = worldIn.getTileEntity(pos);
                if (newTileEntity instanceof TileEntityBlockScript)
                    ((TileEntityBlockScript) newTileEntity).script.scripts.add(script);
            }
        }

        return true;
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    public abstract static class TileLegacyScript<T extends WorldScript<I>, I extends NBTStorable> extends TileEntity implements GeneratingTileEntity<I>
    {
        public T script = createScript();

        public abstract T createScript();

        @Override
        public void readFromNBT(NBTTagCompound nbtTagCompound)
        {
            super.readFromNBT(nbtTagCompound);

            if (nbtTagCompound.hasKey("script"))
                script.readFromNBT(nbtTagCompound.getCompoundTag("script"));
            else // Legacy
                script.readFromNBT(nbtTagCompound);
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound)
        {
            super.writeToNBT(nbtTagCompound);

            NBTTagCompound scriptCompound = new NBTTagCompound();
            script.writeToNBT(scriptCompound);
            nbtTagCompound.setTag("script", scriptCompound);

            return nbtTagCompound;
        }

        @Override
        public I prepareInstanceData(StructurePrepareContext context)
        {
            return script.prepareInstanceData(context, getPos());
        }

        @Override
        public I loadInstanceData(StructureLoadContext context, NBTBase nbt)
        {
            return script.loadInstanceData(context, nbt);
        }

        @Override
        public void generate(StructureSpawnContext context, RunTransformer transformer, I instanceData)
        {
            script.generate(context, transformer, instanceData, pos);
        }
    }
}
