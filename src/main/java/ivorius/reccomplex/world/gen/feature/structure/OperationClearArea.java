/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockPositions;
import ivorius.ivtoolkit.rendering.grid.AreaRenderer;
import ivorius.reccomplex.client.rendering.SelectionRenderer;
import ivorius.reccomplex.operation.Operation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

/**
 * Created by lukas on 11.02.15.
 */
public class OperationClearArea implements Operation
{
    public BlockArea sourceArea;

    public OperationClearArea()
    {
    }

    public OperationClearArea(BlockArea sourceArea)
    {
        this.sourceArea = sourceArea;
    }

    public static void setBlockToAirClean(World world, BlockPos pos)
    {
        emptyOut(world, pos);
        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
    }

    public static void emptyOut(World world, BlockPos pos)
    {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IInventory)
        {
            IInventory inventory = (IInventory) tileEntity;
            for (int i = 0; i < inventory.getSizeInventory(); i++)
                inventory.setInventorySlotContents(i, null);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        BlockPositions.writeToNBT("sourcePoint1", sourceArea.getPoint1(), compound);
        BlockPositions.writeToNBT("sourcePoint2", sourceArea.getPoint2(), compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        sourceArea = new BlockArea(BlockPositions.readFromNBT("sourcePoint1", compound), BlockPositions.readFromNBT("sourcePoint2", compound));
    }

    @Override
    public void perform(WorldServer world)
    {
        for (BlockPos coord : sourceArea)
            setBlockToAirClean(world, coord);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderPreview(PreviewType previewType, World world, int ticks, float partialTicks)
    {
        if (previewType == PreviewType.BOUNDING_BOX || previewType == PreviewType.SHAPE)
        {
            GL11.glLineWidth(3.0f);
            GlStateManager.color(0.5f, 0.5f, 1.0f);
            AreaRenderer.renderAreaLined(sourceArea, 0.0212f);

            GlStateManager.enableBlend();
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0001f);

            ResourceLocation curTex = SelectionRenderer.TEXTURE[MathHelper.floor_float((ticks + partialTicks) * 0.75f) % SelectionRenderer.TEXTURE.length];
            Minecraft.getMinecraft().renderEngine.bindTexture(curTex);

            GlStateManager.color(0.3f, 0.3f, 0.4f, 0.2f);
            AreaRenderer.renderArea(sourceArea, false, true, 0.0112f);

            GlStateManager.color(0.4f, 0.4f, 0.5f, 0.35f);
            AreaRenderer.renderArea(sourceArea, false, false, 0.0112f);

            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.002f);
            GlStateManager.disableBlend();
        }
    }
}
