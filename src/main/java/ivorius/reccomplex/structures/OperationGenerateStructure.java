/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.client.rendering.AreaRenderer;
import ivorius.reccomplex.client.rendering.SelectionRenderer;
import ivorius.reccomplex.operation.Operation;
import ivorius.reccomplex.worldgen.StructureGenerator;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
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

    public static void maybeRenderBoundingBox(BlockCoord lowerCoord, int[] size, int ticks, float partialTicks)
    {
        if (size[0] > 0 && size[1] > 0 && size[2] > 0)
            renderBoundingBox(BlockArea.areaFromSize(lowerCoord, size), ticks, partialTicks);
    }

    public static void renderBoundingBox(BlockArea area, int ticks, float partialTicks)
    {
        GL11.glLineWidth(3.0f);
        GL11.glColor3f(0.8f, 0.8f, 1.0f);
        AreaRenderer.renderAreaLined(area, 0.0232f);

        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.0001f);

        ResourceLocation curTex = SelectionRenderer.textureSelection[MathHelper.floor_float((ticks + partialTicks) * 0.75f) % SelectionRenderer.textureSelection.length];
        Minecraft.getMinecraft().renderEngine.bindTexture(curTex);

        GL11.glColor4f(0.6f, 0.6f, 0.8f, 0.3f);
        AreaRenderer.renderArea(area, false, true, 0.0132f);

        GL11.glColor4f(0.8f, 0.8f, 1.0f, 0.5f);
        AreaRenderer.renderArea(area, false, false, 0.0132f);

        GL11.glAlphaFunc(GL11.GL_GREATER, 0.002f);
        GL11.glDisable(GL11.GL_BLEND);
    }

    @Override
    public void perform(World world)
    {
        if (generateAsSource)
            structure.generate(new StructureSpawnContext(world, world.rand, lowerCoord, transform, 0, true, structure));
        else
            StructureGenerator.generateStructureWithNotifications(structure, world, world.rand, lowerCoord, transform, 0, false);
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
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        structure = StructureRegistry.createStructureFromJSON(compound.getString("structureInfo"));
        structure.worldDataCompound = compound.getCompoundTag("structureData");

        transform = new AxisAlignedTransform2D(compound.getInteger("rotation"), compound.getBoolean("mirrorX"));

        lowerCoord = BlockCoord.readCoordFromNBT("lowerCoord", compound);

        generateAsSource = compound.getBoolean("generateAsSource");
    }

    @Override
    public void renderPreview(int previewType, World world, int ticks, float partialTicks)
    {
        if (previewType == PREVIEW_TYPE_BOUNDING_BOX)
            maybeRenderBoundingBox(lowerCoord, StructureInfos.structureSize(structure, transform), ticks, partialTicks);
    }
}
