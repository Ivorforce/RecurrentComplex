/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.client.rendering;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.items.ItemBlockSelectorFloating;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Created by lukas on 10.02.15.
 */
public class SelectionRenderer
{
    public static ResourceLocation[] textureSelection;

    static
    {
        textureSelection = new ResourceLocation[100];
        for (int i = 0; i < textureSelection.length; i++)
            textureSelection[i] = new ResourceLocation(RecurrentComplex.MODID, String.format("%sselection/selection_%05d.png", RecurrentComplex.filePathTextures, i));
    }

    public static void renderSelection(EntityLivingBase entity, int ticks, float partialTicks)
    {
        Minecraft mc = Minecraft.getMinecraft();
        EntityLivingBase renderEntity = mc.renderViewEntity;
        StructureEntityInfo structureEntityInfo = StructureEntityInfo.getStructureEntityInfo(entity);
        BlockCoord selPoint1 = null;
        BlockCoord selPoint2 = null;

        if (structureEntityInfo != null)
        {
            selPoint1 = structureEntityInfo.selectedPoint1;
            selPoint2 = structureEntityInfo.selectedPoint2;
        }

        ItemStack heldItem = entity.getHeldItem();

        double entityX = renderEntity.lastTickPosX + (renderEntity.posX - renderEntity.lastTickPosX) * (double) partialTicks;
        double entityY = renderEntity.lastTickPosY + (renderEntity.posY - renderEntity.lastTickPosY) * (double) partialTicks;
        double entityZ = renderEntity.lastTickPosZ + (renderEntity.posZ - renderEntity.lastTickPosZ) * (double) partialTicks;

        GL11.glPushMatrix();
        GL11.glTranslated(-entityX, -entityY, -entityZ);

        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glLineWidth(3.0f);

        if (selPoint1 != null)
        {
            GL11.glColor3f(1.0f, 0.6f, 0.6f);
            AreaRenderer.renderArea(new BlockArea(selPoint1, selPoint1), true, 0.03f);
        }
        if (selPoint2 != null)
        {
            GL11.glColor3f(0.6f, 1.0f, 0.6f);
            AreaRenderer.renderArea(new BlockArea(selPoint2, selPoint2), true, 0.04f);
        }

        if (heldItem != null && heldItem.getItem() instanceof ItemBlockSelectorFloating)
        {
            BlockCoord hoverPoint = ItemBlockSelectorFloating.getHoveredBlock(renderEntity, ((ItemBlockSelectorFloating) heldItem.getItem()).selectionRange);
            GL11.glColor3f(0.6f, 0.6f, 1.0f);
            AreaRenderer.renderArea(new BlockArea(hoverPoint, hoverPoint), true, 0.05f);
        }

        if (selPoint1 != null && selPoint2 != null)
        {
            GL11.glColor3f(0.6f, 0.6f, 0.6f);
            AreaRenderer.renderArea(new BlockArea(selPoint1, selPoint2), true, 0.02f);

            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.0001f);

            GL11.glEnable(GL11.GL_TEXTURE_2D);
            ResourceLocation curTex = textureSelection[MathHelper.floor_float((ticks + partialTicks) * 0.75f) % textureSelection.length];
            mc.renderEngine.bindTexture(curTex);

            GL11.glColor4f(0.4f, 0.65f, 0.8f, 0.75f);
            AreaRenderer.renderArea(new BlockArea(selPoint1, selPoint2), false, 0.01f);

            GL11.glAlphaFunc(GL11.GL_GREATER, 0.002f);
            GL11.glDisable(GL11.GL_BLEND);
        }
        else
            GL11.glEnable(GL11.GL_TEXTURE_2D);


        GL11.glPopMatrix();
    }
}
