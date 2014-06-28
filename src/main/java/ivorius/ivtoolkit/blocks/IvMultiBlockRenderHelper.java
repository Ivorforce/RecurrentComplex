/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.ivtoolkit.blocks;

import org.lwjgl.opengl.GL11;

/**
 * Created by lukas on 23.04.14.
 */
public class IvMultiBlockRenderHelper
{
    public static void transformFor(IvTileEntityMultiBlock tileEntity, double renderX, double renderY, double renderZ)
    {
        double[] center = tileEntity.getActiveCenterCoords();

        GL11.glTranslated(renderX + center[0] - tileEntity.xCoord, renderY + center[1] - tileEntity.yCoord, renderZ + center[2] - tileEntity.zCoord);
        GL11.glRotatef(-90.0f * tileEntity.direction + 180.0f, 0.0f, 1.0f, 0.0f);
    }
}
