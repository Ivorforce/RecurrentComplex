/*
 * Copyright (c) 2014, Lukas Tenbrink.
 * http://lukas.axxim.net
 *
 * You are free to:
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes, unless you have a permit by the creator.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package ivorius.ivtoolkit.blocks;

import org.lwjgl.opengl.GL11;

/**
 * Created by lukas on 26.07.14.
 */
public class IvRotatableBlockRenderHelper
{
    public static void transformFor(IvTileEntityRotatable tileEntity, double renderX, double renderY, double renderZ)
    {
        GL11.glTranslated(renderX + 0.5, renderY + 0.5, renderZ + 0.5);
        GL11.glRotatef(-90.0f * tileEntity.direction + 180.0f, 0.0f, 1.0f, 0.0f);
    }
}
