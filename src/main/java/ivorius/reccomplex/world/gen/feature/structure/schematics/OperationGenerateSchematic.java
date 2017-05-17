/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.schematics;

import ivorius.ivtoolkit.blocks.BlockPositions;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.utils.RCAxisAlignedTransform;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by lukas on 10.02.15.
 */
public class OperationGenerateSchematic implements Operation
{
    public SchematicFile file;

    public AxisAlignedTransform2D transform;
    public BlockPos lowerCoord;

    protected GridQuadCache cachedShapeGrid;

    public OperationGenerateSchematic()
    {
    }

    public OperationGenerateSchematic(SchematicFile file, AxisAlignedTransform2D transform, BlockPos lowerCoord)
    {
        this.file = file;
        this.transform = transform;
        this.lowerCoord = lowerCoord;
    }

    @Override
    public void perform(WorldServer world)
    {
        if (lowerCoord != null)
            file.generate(world, lowerCoord, transform);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        NBTTagCompound fileCompound = new NBTTagCompound();
        file.writeToNBT(fileCompound);
        compound.setTag("schematic", fileCompound);

        compound.setInteger("rotation", transform.getRotation());
        compound.setBoolean("mirrorX", transform.isMirrorX());

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
            RecurrentComplex.logger.error(e);
            file = new SchematicFile((short) 0, (short) 0, (short) 0);
        }

        transform = AxisAlignedTransform2D.from(compound.getInteger("rotation"), compound.getBoolean("mirrorX"));

        lowerCoord = BlockPositions.readFromNBT("lowerCoord", compound);
    }

    public void invalidateCache()
    {
        cachedShapeGrid = null;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderPreview(PreviewType previewType, World world, int ticks, float partialTicks)
    {
        if (lowerCoord != null)
        {
            int[] size = {file.width, file.height, file.length};
            if (previewType == PreviewType.SHAPE)
            {
                GlStateManager.color(0.8f, 0.75f, 1.0f);
                OperationRenderer.renderGridQuadCache(
                        cachedShapeGrid != null ? cachedShapeGrid : (cachedShapeGrid = SchematicQuadCache.createQuadCache(file, new float[]{1, 1, 1})),
                        transform, lowerCoord, ticks, partialTicks);
            }

            if (previewType == PreviewType.BOUNDING_BOX || previewType == PreviewType.SHAPE)
                OperationRenderer.renderBoundingBox(OperationRenderer.blockAreaFromSize(lowerCoord, RCAxisAlignedTransform.applySize(transform, size)), ticks, partialTicks);
        }
    }
}
