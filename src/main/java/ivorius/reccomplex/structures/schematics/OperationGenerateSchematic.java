/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.schematics;

import ivorius.ivtoolkit.blocks.BlockPositions;
import net.minecraft.util.math.BlockPos;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.rendering.grid.GridQuadCache;
import ivorius.reccomplex.client.rendering.OperationRenderer;
import ivorius.reccomplex.client.rendering.SchematicQuadCache;
import ivorius.reccomplex.operation.Operation;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.world.WorldServer;

/**
 * Created by lukas on 10.02.15.
 */
public class OperationGenerateSchematic implements Operation
{
    public SchematicFile file;

    public BlockPos lowerCoord;

    protected GridQuadCache cachedShapeGrid;

    public OperationGenerateSchematic()
    {
    }

    public OperationGenerateSchematic(SchematicFile file, BlockPos lowerCoord)
    {
        this.file = file;
        this.lowerCoord = lowerCoord;
    }

    @Override
    public void perform(WorldServer world)
    {
        file.generate(world, lowerCoord);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        NBTTagCompound fileCompound = new NBTTagCompound();
        file.writeToNBT(fileCompound);
        compound.setTag("schematic", fileCompound);

        BlockPositions.writeToNBT("lowerCoord", lowerCoord, compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        try
        {
            file = new SchematicFile(compound.getCompoundTag("schematic"));
        }
        catch (SchematicFile.UnsupportedSchematicFormatException e)
        {
            e.printStackTrace();
            file = new SchematicFile((short) 0, (short) 0, (short) 0);
        }

        lowerCoord = BlockPositions.readFromNBT("lowerCoord", compound);
    }

    public void invalidateCache()
    {
        cachedShapeGrid = null;
    }

    @Override
    public void renderPreview(PreviewType previewType, World world, int ticks, float partialTicks)
    {
        int[] size = {file.width, file.height, file.length};
        if (previewType == PreviewType.SHAPE)
        {
            GlStateManager.color(0.8f, 0.75f, 1.0f);
            OperationRenderer.renderGridQuadCache(
                    cachedShapeGrid != null ? cachedShapeGrid : (cachedShapeGrid = SchematicQuadCache.createQuadCache(file, new float[]{1, 1, 1})),
                    AxisAlignedTransform2D.ORIGINAL, lowerCoord, ticks, partialTicks);
        }

        if (previewType == PreviewType.BOUNDING_BOX || previewType == PreviewType.SHAPE)
            OperationRenderer.maybeRenderBoundingBox(lowerCoord, size, ticks, partialTicks);
    }
}
