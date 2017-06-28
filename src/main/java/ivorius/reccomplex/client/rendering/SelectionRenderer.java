/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.client.rendering;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.rendering.grid.AreaRenderer;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.item.ItemBlockSelector;
import ivorius.reccomplex.item.ItemBlockSelectorFloating;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

/**
 * Created by lukas on 10.02.15.
 */
public class SelectionRenderer
{
    public static ResourceLocation[] TEXTURE;
    public static ResourceLocation[] LATTICE_TEXTURE;

    static
    {
        TEXTURE = new ResourceLocation[100];
        for (int i = 0; i < TEXTURE.length; i++)
            TEXTURE[i] = new ResourceLocation(RecurrentComplex.MOD_ID, String.format("%sselection/selection_%05d.png", RecurrentComplex.filePathTextures, i));

        LATTICE_TEXTURE = new ResourceLocation[100];
        for (int i = 0; i < LATTICE_TEXTURE.length; i++)
            LATTICE_TEXTURE[i] = new ResourceLocation(RecurrentComplex.MOD_ID, String.format("%sselection-lattice/lattice_%05d.png", RecurrentComplex.filePathTextures, i));
    }

    public static void renderSelection(EntityLivingBase entity, int ticks, float partialTicks)
    {
        Minecraft mc = Minecraft.getMinecraft();
        BlockPos selPoint1 = null;
        BlockPos selPoint2 = null;

        SelectionOwner owner = SelectionOwner.getOwner(entity, null);
        if (owner != null)
        {
            selPoint1 = owner.getSelectedPoint1();
            selPoint2 = owner.getSelectedPoint2();
        }

        GL11.glLineWidth(3.0f);

        if (selPoint1 != null)
        {
            GlStateManager.color(0.6f, 0.8f, 0.95f);
            AreaRenderer.renderAreaLined(new BlockArea(selPoint1, selPoint1), 0.03f);
        }
        if (selPoint2 != null)
        {
            GlStateManager.color(0.2f, 0.45f, 0.65f);
            AreaRenderer.renderAreaLined(new BlockArea(selPoint2, selPoint2), 0.04f);
        }

        for (EnumHand enumHand : EnumHand.values())
        {
            ItemStack heldItem = entity.getHeldItem(enumHand);

            if (heldItem != null && heldItem.getItem() instanceof ItemBlockSelector)
            {
                ItemBlockSelector selector = (ItemBlockSelector) heldItem.getItem();
                BlockPos hoverPoint = selector.hoveredBlock(heldItem, entity);
                GlStateManager.color(0.6f, 0.6f, 1.0f);
                AreaRenderer.renderAreaLined(new BlockArea(hoverPoint, hoverPoint), 0.05f);
            }
        }

        if (selPoint1 != null && selPoint2 != null)
        {
            BlockArea selArea = new BlockArea(selPoint1, selPoint2);

            GlStateManager.color(0.4f, 0.65f, 0.8f);
            AreaRenderer.renderAreaLined(selArea, 0.02f);

            GlStateManager.enableBlend();
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0001f);

            ResourceLocation curTex = TEXTURE[MathHelper.floor_float((ticks + partialTicks) * 0.75f) % TEXTURE.length];
            mc.renderEngine.bindTexture(curTex);

            GlStateManager.color(0.2f, 0.5f, 0.6f, 0.5f);
            AreaRenderer.renderArea(selArea, false, true, 0.01f);

            GlStateManager.color(0.4f, 0.65f, 0.8f, 0.75f);
            AreaRenderer.renderArea(selArea, false, false, 0.01f);

            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.002f);
            GlStateManager.disableBlend();
        }
    }
}
