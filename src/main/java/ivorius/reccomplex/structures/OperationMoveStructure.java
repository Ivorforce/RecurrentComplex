/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.rendering.grid.AreaRenderer;
import ivorius.ivtoolkit.rendering.grid.BlockQuadCache;
import ivorius.reccomplex.client.rendering.OperationRenderer;
import ivorius.reccomplex.client.rendering.SelectionRenderer;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

/**
 * Created by lukas on 11.02.15.
 */
public class OperationMoveStructure extends OperationGenerateStructure
{
    public BlockArea sourceArea;

    public OperationMoveStructure()
    {
    }

    public OperationMoveStructure(GenericStructureInfo structure, AxisAlignedTransform2D transform, BlockCoord lowerCoord, boolean generateAsSource, BlockArea sourceArea)
    {
        super(structure, transform, lowerCoord, generateAsSource);
        this.sourceArea = sourceArea;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        BlockCoord.writeCoordToNBT("sourcePoint1", sourceArea.getPoint1(), compound);
        BlockCoord.writeCoordToNBT("sourcePoint2", sourceArea.getPoint2(), compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        sourceArea = new BlockArea(BlockCoord.readCoordFromNBT("sourcePoint1", compound), BlockCoord.readCoordFromNBT("sourcePoint2", compound));
    }

    @Override
    public void perform(World world)
    {
        for (BlockCoord coord : sourceArea)
            setBlockToAirClean(world, coord);

        super.perform(world);
    }

    public static void setBlockToAirClean(World world, BlockCoord blockCoord)
    {
        TileEntity tileEntity = world.getTileEntity(blockCoord.x, blockCoord.y, blockCoord.z);
        if (tileEntity instanceof IInventory)
        {
            IInventory inventory = (IInventory) tileEntity;
            for (int i = 0; i < inventory.getSizeInventory(); i++)
                inventory.setInventorySlotContents(i, null);
        }

        world.setBlockToAir(blockCoord.x, blockCoord.y, blockCoord.z);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderPreview(PreviewType previewType, World world, int ticks, float partialTicks)
    {
        int[] size = structure.structureBoundingBox();
        if (previewType == PreviewType.SHAPE)
        {
            GL11.glColor3f(0.8f, 0.75f, 1.0f);
            OperationRenderer.renderGridQuadCache(
                    cachedShapeGrid != null ? cachedShapeGrid : (cachedShapeGrid = BlockQuadCache.createQuadCache(structure.constructWorldData(world).blockCollection, new float[]{1, 1, 1})),
                    transform, lowerCoord, ticks, partialTicks);
        }

        if (previewType == PreviewType.BOUNDING_BOX || previewType == PreviewType.SHAPE)
        {
            OperationRenderer.maybeRenderBoundingBox(lowerCoord, StructureInfos.structureSize(size, transform), ticks, partialTicks);

            GL11.glLineWidth(3.0f);
            GL11.glColor3f(0.5f, 0.5f, 1.0f);
            AreaRenderer.renderAreaLined(sourceArea, 0.0212f);

            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.0001f);

            ResourceLocation curTex = SelectionRenderer.TEXTURE[MathHelper.floor_float((ticks + partialTicks) * 0.75f) % SelectionRenderer.TEXTURE.length];
            Minecraft.getMinecraft().renderEngine.bindTexture(curTex);

            GL11.glColor4f(0.3f, 0.3f, 0.4f, 0.2f);
            AreaRenderer.renderArea(sourceArea, false, true, 0.0112f);

            GL11.glColor4f(0.4f, 0.4f, 0.5f, 0.35f);
            AreaRenderer.renderArea(sourceArea, false, false, 0.0112f);

            GL11.glAlphaFunc(GL11.GL_GREATER, 0.002f);
            GL11.glDisable(GL11.GL_BLEND);
        }
    }
}
