/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures;

import ivorius.ivtoolkit.blocks.BlockPositions;
import net.minecraft.util.math.BlockPos;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.rendering.grid.BlockQuadCache;
import ivorius.ivtoolkit.rendering.grid.GridQuadCache;
import ivorius.reccomplex.client.rendering.*;
import ivorius.reccomplex.operation.Operation;
import ivorius.reccomplex.worldgen.StructureGenerator;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;
import net.minecraft.client.renderer.GlStateManager;

/**
 * Created by lukas on 10.02.15.
 */
public class OperationGenerateStructure implements Operation
{
    public GenericStructureInfo structure;

    public AxisAlignedTransform2D transform;
    public BlockPos lowerCoord;

    public boolean generateAsSource;

    public String structureIDForSaving;

    protected GridQuadCache cachedShapeGrid;

    public OperationGenerateStructure()
    {
    }

    public OperationGenerateStructure(GenericStructureInfo structure, AxisAlignedTransform2D transform, BlockPos lowerCoord, boolean generateAsSource)
    {
        this.structure = structure;
        this.transform = transform;
        this.lowerCoord = lowerCoord;
        this.generateAsSource = generateAsSource;
    }

    public OperationGenerateStructure(GenericStructureInfo structure, AxisAlignedTransform2D transform, BlockPos lowerCoord, boolean generateAsSource, String structureIDForSaving)
    {
        this.structure = structure;
        this.transform = transform;
        this.lowerCoord = lowerCoord;
        this.generateAsSource = generateAsSource;
        this.structureIDForSaving = structureIDForSaving;
    }

    public String getStructureIDForSaving()
    {
        return structureIDForSaving;
    }

    public void setStructureIDForSaving(String structureIDForSaving)
    {
        this.structureIDForSaving = structureIDForSaving;
    }

    @Override
    public void perform(WorldServer world)
    {
        if (generateAsSource)
            StructureGenerator.directly(structure, StructureSpawnContext.complete(world, world.rand, transform, lowerCoord, structure, 0, true));
        else
            StructureGenerator.instantly(structure, world, world.rand, lowerCoord, transform, 0, false, structureIDForSaving, false);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        compound.setString("structureInfo", StructureRegistry.INSTANCE.createJSONFromStructure(structure));
        compound.setTag("structureData", structure.worldDataCompound);

        compound.setInteger("rotation", transform.getRotation());
        compound.setBoolean("mirrorX", transform.isMirrorX());

        BlockPositions.writeToNBT("lowerCoord", lowerCoord, compound);

        compound.setBoolean("generateAsSource", generateAsSource);

        if (structureIDForSaving != null)
            compound.setString("structureIDForSaving", structureIDForSaving);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        structure = StructureRegistry.INSTANCE.createStructureFromJSON(compound.getString("structureInfo"));
        structure.worldDataCompound = compound.getCompoundTag("structureData");

        transform = AxisAlignedTransform2D.from(compound.getInteger("rotation"), compound.getBoolean("mirrorX"));

        lowerCoord = BlockPositions.readFromNBT("lowerCoord", compound);

        generateAsSource = compound.getBoolean("generateAsSource");

        structureIDForSaving = compound.hasKey("structureIDForSaving", Constants.NBT.TAG_STRING)
                ? compound.getString("structureIDForSaving")
                : null;
    }

    public void invalidateCache()
    {
        cachedShapeGrid = null;
    }

    @Override
    public void renderPreview(PreviewType previewType, World world, int ticks, float partialTicks)
    {
        int[] size = structure.structureBoundingBox();
        if (previewType == PreviewType.SHAPE)
        {
            GlStateManager.color(0.8f, 0.75f, 1.0f);
            OperationRenderer.renderGridQuadCache(
                    cachedShapeGrid != null ? cachedShapeGrid : (cachedShapeGrid = BlockQuadCache.createQuadCache(structure.constructWorldData().blockCollection, new float[]{1, 1, 1})),
                    transform, lowerCoord, ticks, partialTicks);
        }

        if (previewType == PreviewType.BOUNDING_BOX || previewType == PreviewType.SHAPE)
            OperationRenderer.maybeRenderBoundingBox(lowerCoord, StructureInfos.structureSize(size, transform), ticks, partialTicks);
    }
}
