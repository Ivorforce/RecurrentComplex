/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui;

import ivorius.reccomplex.RecurrentComplex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

/**
 * Created by lukas on 12.06.14.
 */
public class GuiValidityStateIndicator extends Gui
{
    protected static final ResourceLocation textureSprite = new ResourceLocation(RecurrentComplex.MODID, RecurrentComplex.filePathTextures + "guiStates.png");
    /**
     * Button width in pixels
     */
    protected int width = 10;
    /**
     * Button height in pixels
     */
    protected int height = 10;
    /**
     * The x position of this control.
     */
    public int xPosition;
    /**
     * The y position of this control.
     */
    public int yPosition;

    protected State state;

    private boolean isVisible = true;

    public GuiValidityStateIndicator(int xPosition, int yPosition, State state)
    {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.state = state;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public int getxPosition()
    {
        return xPosition;
    }

    public int getyPosition()
    {
        return yPosition;
    }

    public void draw()
    {
        if (isVisible)
        {
            GlStateManager.color(1.0f, 1.0f, 1.0f);
            Minecraft.getMinecraft().getTextureManager().bindTexture(textureSprite);
            drawTexturedModalRect(xPosition, yPosition, state.getUvX(), state.getUvY(), width, height);
        }
    }

    public State getState()
    {
        return state;
    }

    public void setState(State state)
    {
        this.state = state;
    }

    public boolean isVisible()
    {
        return isVisible;
    }

    public void setVisible(boolean isVisible)
    {
        this.isVisible = isVisible;
    }

    public enum State
    {
        VALID(0, 0),
        SEMI_VALID(10, 0),
        INVALID(20, 0),
        UNKNWON(30, 0);

        private int uvX;
        private int uvY;

        State(int uvX, int uvY)
        {
            this.uvX = uvX;
            this.uvY = uvY;
        }

        public int getUvX()
        {
            return uvX;
        }

        public int getUvY()
        {
            return uvY;
        }
    }
}
