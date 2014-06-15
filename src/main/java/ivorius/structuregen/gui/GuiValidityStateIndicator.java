/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.gui;

import ivorius.structuregen.StructureGen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Created by lukas on 12.06.14.
 */
public class GuiValidityStateIndicator extends Gui
{
    protected static final ResourceLocation textureSprite = new ResourceLocation(StructureGen.MODID, StructureGen.filePathTextures + "guiStates.png");
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

    public void draw()
    {
        if (isVisible)
        {
            GL11.glColor3f(1.0f, 1.0f, 1.0f);
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

    public static enum State
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
