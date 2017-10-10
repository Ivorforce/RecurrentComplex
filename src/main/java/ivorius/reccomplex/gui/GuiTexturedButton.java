/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.table.Bounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Created by lukas on 02.11.16.
 */
public class GuiTexturedButton extends GuiButton
{
    private ResourceLocation texture;
    private int textureWidth;
    private int textureHeight;

    public GuiTexturedButton(int buttonId, int x, int y, ResourceLocation texture)
    {
        super(buttonId, x, y, "");
        setTexture(texture);
    }

    public GuiTexturedButton(int buttonId, int x, int y, int widthIn, int heightIn, ResourceLocation texture)
    {
        super(buttonId, x, y, widthIn, heightIn, "");
        setTexture(texture);
    }

    public GuiTexturedButton(int buttonId, int x, int y, String buttonText)
    {
        super(buttonId, x, y, buttonText);
    }

    public GuiTexturedButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText)
    {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
    }

    public static void setBounds(Bounds bounds, GuiButton button)
    {
        button.x = bounds.getMinX();
        button.width = bounds.getWidth();
        button.y = bounds.getMinY();
        button.height = bounds.getHeight();
    }

    public ResourceLocation getTexture()
    {
        return texture;
    }

    public void setTexture(ResourceLocation texture)
    {
        this.texture = texture;

        if (texture != null)
        {
            try
            {
                IResource iresource = Minecraft.getMinecraft().getResourceManager().getResource(texture);
                BufferedImage bufferedimage = TextureUtil.readBufferedImage(iresource.getInputStream());
                textureWidth = bufferedimage.getWidth();
                textureHeight = bufferedimage.getHeight();
            }
            catch (IOException e)
            {
                RecurrentComplex.logger.error(e);
                this.texture = null;
            }
        }
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
    {
        super.drawButton(mc, mouseX, mouseY, partialTicks);

        if (this.visible && texture != null)
        {
            mc.getTextureManager().bindTexture(texture);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            this.drawTexture(this.x + (width - textureWidth) / 2, this.y + (height - textureHeight) / 2, 0, 0, textureWidth, textureHeight);
        }
    }

    public void drawTexture(int x, int y, int textureX, int textureY, int width, int height)
    {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        vertexbuffer.pos((double)(x + 0), (double)(y + height), (double)this.zLevel).tex((double)((float)(textureX + 0) / textureWidth), (double)((float)(textureY + height) / textureHeight)).endVertex();
        vertexbuffer.pos((double)(x + width), (double)(y + height), (double)this.zLevel).tex((double)((float)(textureX + width) / textureWidth), (double)((float)(textureY + height) / textureHeight)).endVertex();
        vertexbuffer.pos((double)(x + width), (double)(y + 0), (double)this.zLevel).tex((double)((float)(textureX + width) / textureWidth), (double)((float)(textureY + 0) / textureHeight)).endVertex();
        vertexbuffer.pos((double)(x + 0), (double)(y + 0), (double)this.zLevel).tex((double)((float)(textureX + 0) / textureWidth), (double)((float)(textureY + 0) / textureHeight)).endVertex();
        tessellator.draw();
    }
}
