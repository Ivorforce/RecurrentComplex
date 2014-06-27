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

package ivorius.ivtoolkit.rendering;

import org.apache.logging.log4j.Logger;

/**
 * Created by lukas on 18.02.14.
 */
public abstract class IvShaderInstance2D extends IvShaderInstance implements Iv2DScreenEffect
{
    public IvShaderInstance2D(Logger logger)
    {
        super(logger);
    }

    @Override
    public boolean shouldApply(float ticks)
    {
        return getShaderID() > 0;
    }

    @Override
    public abstract void apply(int screenWidth, int screenHeight, float ticks, IvOpenGLTexturePingPong pingPong);

    public void drawFullScreen(int screenWidth, int screenHeight, IvOpenGLTexturePingPong pingPong)
    {
        pingPong.pingPong();
        IvRenderHelper.drawRectFullScreen(screenWidth, screenHeight);
    }

    @Override
    public void destruct()
    {
        deleteShader();
    }
}
