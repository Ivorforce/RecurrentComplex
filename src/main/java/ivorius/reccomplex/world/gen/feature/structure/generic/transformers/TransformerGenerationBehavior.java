/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.transformers;

import ivorius.ivtoolkit.blocks.BlockPositions;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.ivtoolkit.blocks.IvTileEntityHelper;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.ivtoolkit.tools.NBTTagLists;
import ivorius.ivtoolkit.world.chunk.gen.StructureBoundingBoxes;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.block.GeneratingTileEntity;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.nbt.NBTStorable;
import ivorius.reccomplex.temp.RCMover;
import ivorius.reccomplex.temp.RCPosTransformer;
import ivorius.reccomplex.utils.UnstableBlock;
import ivorius.reccomplex.world.gen.feature.structure.context.*;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class TransformerGenerationBehavior extends Transformer<TransformerGenerationBehavior.InstanceData>
{
    public TransformerGenerationBehavior()
    {
        super("GenerationBehavior");
    }

    public static void asGeneratingTileEntity(@Nonnull StructureContext context, World world, int[] areaSize, BlockPos origin, NBTTagCompound tileEntityCompound, BiConsumer<BlockPos, GeneratingTileEntity> consumer)
    {
        BlockPos src = RCMover.getTileEntityPos(tileEntityCompound);
        BlockPos dest = context.transform.apply(src, areaSize).add(origin);

        tileEntityCompound = RCMover.setTileEntityPos(tileEntityCompound, dest);

        TileEntity tileEntity = RecurrentComplex.specialRegistry.loadTileEntity(world, tileEntityCompound);
        if (tileEntity instanceof GeneratingTileEntity) {
            RCPosTransformer.transformAdditionalData(tileEntity, context.transform, areaSize);
            RCMover.moveAdditionalData(tileEntity, origin);

            consumer.accept(src, (GeneratingTileEntity) tileEntity);
        }
    }

    @Override
    public void transform(InstanceData instanceData, Phase phase, StructureSpawnContext context, IvWorldData worldData, RunTransformer transformer)
    {
        if (phase != Phase.AFTER) {
            return;
        }

        BlockPos origin = context.lowerCoord();
        int[] areaSize = new int[]{worldData.blockCollection.width, worldData.blockCollection.height, worldData.blockCollection.length};
        WorldServer world = context.environment.world;

        if (context.generationLayer >= GenericStructure.MAX_GENERATING_LAYERS) {
            RecurrentComplex.logger.warn("Structure generated with over max layers; most likely infinite loop!");
            return;
        }

        for (NBTTagCompound tileEntityCompound : worldData.tileEntities) {
            asGeneratingTileEntity(context, world, areaSize, origin, tileEntityCompound, (blockPos, tileEntity) -> {
                NBTStorable teData = instanceData.tileEntities.get(blockPos);
                if (teData != null) // Otherwise it was added after prepare, or doesn't want to generate
                    //noinspection unchecked
                    tileEntity.generate(context, transformer, teData);
            });
        }
    }

    @Override
    public String getDisplayString()
    {
        throw new IllegalStateException();
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        throw new IllegalStateException();
    }

    @Override
    public boolean skipGeneration(InstanceData instanceData, StructureLiveContext context, BlockPos pos, IBlockState state, IvWorldData worldData, BlockPos sourcePos)
    {
        // Block
        return UnstableBlock.shouldSkipState(state) && !instanceData.allowedGTECoords.contains(sourcePos);
    }

    @Override
    public TransformerGenerationBehavior.InstanceData prepareInstanceData(StructurePrepareContext context, IvWorldData worldData)
    {
        InstanceData instanceData = new InstanceData();

        int[] areaSize = new int[]{worldData.blockCollection.width, worldData.blockCollection.height, worldData.blockCollection.length};
        BlockPos origin = context.lowerCoord();

        worldData.tileEntities.forEach(tileEntityCompound ->
        {
            asGeneratingTileEntity(context, IvTileEntityHelper.getAnyWorld(), areaSize, origin, tileEntityCompound, (src, tileEntity) ->
            {
                NBTStorable tileEntityInstanceData = (NBTStorable) tileEntity.prepareInstanceData(context);
                if (tileEntityInstanceData != null) // Otherwise, don't generate
                {
                    instanceData.tileEntities.put(src, tileEntityInstanceData);

                    //noinspection unchecked
                    if (tileEntity.shouldPlaceInWorld(context, tileEntityInstanceData)) {
                        instanceData.allowedGTECoords.add(src);
                    }
                }
            });
        });

        return instanceData;
    }

    @Override
    public TransformerGenerationBehavior.InstanceData loadInstanceData(StructureLoadContext context, NBTBase nbt)
    {
        throw new IllegalStateException();
    }

    public static class InstanceData implements NBTStorable
    {
        public static final String KEY_TILE_ENTITIES = "tileEntities";

        public final Map<BlockPos, NBTStorable> tileEntities = new HashMap<>();
        public final Set<BlockPos> allowedGTECoords = new HashSet<>();

        protected static NBTBase getTileEntityTag(NBTTagCompound tileEntityCompound, BlockPos coord)
        {
            return tileEntityCompound.getTag(getTileEntityKey(coord));
        }

        private static String getTileEntityKey(BlockPos coord)
        {
            return String.format("%d,%d,%d", coord.getX(), coord.getY(), coord.getZ());
        }

        public void readFromNBT(StructureLoadContext context, NBTBase nbt, IvWorldData worldData)
        {
            IvBlockCollection blockCollection = worldData.blockCollection;
            NBTTagCompound compound = nbt instanceof NBTTagCompound ? (NBTTagCompound) nbt : new NBTTagCompound();

            int[] areaSize = new int[]{blockCollection.width, blockCollection.height, blockCollection.length};
            BlockPos origin = StructureBoundingBoxes.min(context.boundingBox);

            NBTTagCompound tileEntitiesCompound = compound.getCompoundTag(KEY_TILE_ENTITIES);
            worldData.tileEntities.forEach(tileEntityCompound ->
            {
                asGeneratingTileEntity(context, IvTileEntityHelper.getAnyWorld(), areaSize, origin, tileEntityCompound, (src, tileEntity) ->
                {
                    tileEntities.put(src, (NBTStorable) (tileEntity.loadInstanceData(context, getTileEntityTag(tileEntitiesCompound, src))));
                });
            });

            allowedGTECoords.clear();
            allowedGTECoords.addAll(NBTTagLists.intArraysFrom(compound, "allowedCoords").stream()
                    .map(BlockPositions::fromIntArray)
                    .collect(Collectors.toSet()));
        }

        @Override
        public NBTBase writeToNBT()
        {
            NBTTagCompound compound = new NBTTagCompound();

            NBTTagCompound tileEntityCompound = new NBTTagCompound();
            for (Map.Entry<BlockPos, NBTStorable> entry : tileEntities.entrySet())
                tileEntityCompound.setTag(getTileEntityKey(entry.getKey()), entry.getValue().writeToNBT());
            compound.setTag(KEY_TILE_ENTITIES, tileEntityCompound);

            NBTTagLists.writeIntArraysTo(compound, "allowedGTECoords", allowedGTECoords.stream()
                    .map(BlockPositions::toIntArray)
                    .collect(Collectors.toList()));

            return compound;
        }
    }
}
