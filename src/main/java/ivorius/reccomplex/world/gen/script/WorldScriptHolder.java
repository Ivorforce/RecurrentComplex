/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.script;

import ivorius.ivtoolkit.blocks.BlockPositions;
import ivorius.ivtoolkit.blocks.BlockStates;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.worldscripts.TableDataSourceWorldScriptHolder;
import ivorius.reccomplex.nbt.NBTNone;
import ivorius.reccomplex.world.gen.feature.StructureGenerator;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureLoadContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructurePrepareContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureSpawnContext;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import ivorius.reccomplex.world.gen.feature.structure.generic.transformers.RunTransformer;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class WorldScriptHolder implements WorldScript<NBTNone>
{
    @Nullable
    public NBTTagCompound worldData;
    public BlockPos origin = BlockPos.ORIGIN;

    public IBlockState replaceState = Blocks.AIR.getDefaultState();

    public WorldScriptHolder()
    {
    }

    public static void writeBlockState(NBTTagCompound compound, IBlockState state, String id, String meta)
    {
        compound.setString(id, Block.REGISTRY.getNameForObject(state.getBlock()).toString());
        compound.setByte(meta, (byte) BlockStates.toMetadata(state));
    }

    public static IBlockState readBlockState(NBTTagCompound compound, MCRegistry registry, String id, String meta)
    {
        Block block = registry.blockFromID(new ResourceLocation(compound.getString(id)));
        return block != null ? BlockStates.fromMetadata(block, compound.getByte(meta)) : Blocks.AIR.getDefaultState();
    }

    @Override
    public NBTNone prepareInstanceData(StructurePrepareContext context, BlockPos pos)
    {
        return new NBTNone();
    }

    @Override
    public NBTNone loadInstanceData(StructureLoadContext context, NBTBase nbt)
    {
        return new NBTNone();
    }

    @Override
    public void generate(StructureSpawnContext context, RunTransformer transformer, NBTNone instanceData, BlockPos pos)
    {
        if (worldData == null) {
            return;
        }

        GenericStructure structure = new GenericStructure();
        structure.worldDataCompound = worldData.copy();

        int[] strucSize = structure.size();
        BlockPos strucCoord = context.transform.apply(origin, new int[]{1, 1, 1})
                .subtract(context.transform.apply(BlockPos.ORIGIN, strucSize)).add(pos);

        new StructureGenerator<>(structure)
                .asChild(context)
                .transformer(transformer)
                .lowerCoord(strucCoord)
                .generationPredicate(p -> !p.equals(pos))
                .generate();

        context.setBlock(pos, replaceState, 2);
    }

    @Override
    public String getDisplayString()
    {
        return "Holder";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public TableDataSource tableDataSource(BlockPos realWorldPos, TableNavigator navigator, TableDelegate tableDelegate)
    {
        return new TableDataSourceWorldScriptHolder(this, realWorldPos, navigator, tableDelegate);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        worldData = compound.hasKey("worldData") ? compound.getCompoundTag("worldData") : null;
        origin = BlockPositions.readFromNBT("origin", compound);

        replaceState = readBlockState(compound, RecurrentComplex.specialRegistry, "selfID", "selfMeta");
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        if (worldData != null)
            compound.setTag("worldData", worldData.copy());
        BlockPositions.writeToNBT("origin", origin, compound);

        writeBlockState(compound, replaceState, "selfID", "selfMeta");
    }
}
