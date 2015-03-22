/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures;

import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.client.rendering.*;
import ivorius.reccomplex.operation.Operation;
import ivorius.reccomplex.worldgen.StructureGenerator;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.lwjgl.opengl.GL11;

/**
 * Created by lukas on 10.02.15.
 */
public class OperationGenerateStructure implements Operation
{
    public GenericStructureInfo structure;

    public AxisAlignedTransform2D transform;
    public BlockCoord lowerCoord;

    public boolean generateAsSource;

    public String structureIDForSaving;

    protected GridQuadCache cachedShapeGrid;

    public OperationGenerateStructure()
    {
    }

    public OperationGenerateStructure(GenericStructureInfo structure, AxisAlignedTransform2D transform, BlockCoord lowerCoord, boolean generateAsSource)
    {
        this.structure = structure;
        this.transform = transform;
        this.lowerCoord = lowerCoord;
        this.generateAsSource = generateAsSource;
    }

    public OperationGenerateStructure(GenericStructureInfo structure, AxisAlignedTransform2D transform, BlockCoord lowerCoord, boolean generateAsSource, String structureIDForSaving)
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
    public void perform(World world)
    {
        if (generateAsSource)
            structure.generate(new StructureSpawnContext(world, world.rand, lowerCoord, transform, 0, true, structure));
        else
            StructureGenerator.generateStructureWithNotifications(structure, world, world.rand, lowerCoord, transform, 0, false, structureIDForSaving);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        compound.setString("structureInfo", StructureRegistry.createJSONFromStructure(structure));
        compound.setTag("structureData", structure.worldDataCompound);

        compound.setInteger("rotation", transform.getRotation());
        compound.setBoolean("mirrorX", transform.isMirrorX());

        BlockCoord.writeCoordToNBT("lowerCoord", lowerCoord, compound);

        compound.setBoolean("generateAsSource", generateAsSource);

        if (structureIDForSaving != null)
            compound.setString("structureIDForSaving", structureIDForSaving);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        structure = StructureRegistry.createStructureFromJSON(compound.getString("structureInfo"));
        structure.worldDataCompound = compound.getCompoundTag("structureData");

        transform = new AxisAlignedTransform2D(compound.getInteger("rotation"), compound.getBoolean("mirrorX"));

        lowerCoord = BlockCoord.readCoordFromNBT("lowerCoord", compound);

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
            GL11.glColor3f(0.8f, 0.75f, 1.0f);
            OperationRenderer.renderGridQuadCache(
                    cachedShapeGrid != null ? cachedShapeGrid : (cachedShapeGrid = BlockQuadCache.createQuadCache(structure.constructWorldData(world).blockCollection, transform, new float[]{1, 1, 1})),
                    lowerCoord, ticks, partialTicks);
        }

        if (previewType == PreviewType.BOUNDING_BOX || previewType == PreviewType.SHAPE)
            OperationRenderer.maybeRenderBoundingBox(lowerCoord, StructureInfos.structureSize(size, transform), ticks, partialTicks);
    }
}
