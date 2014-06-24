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

package ivorius.structuregen.ivtoolkit.rendering;

import net.minecraft.client.renderer.OpenGlHelper;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.GL11.*;

public class IvOpenGLTexturePingPong
{
    public Logger logger;

    public int[] cacheTextures = new int[2];
    public int activeBuffer;

    public int pingPongFB;
    public boolean setup = false;
    public boolean setupRealtimeFB = false;
    public boolean setupCacheTextureForTick = false;

    private int screenWidth;
    private int screenHeight;

    private int parentFrameBuffer;

    private boolean useFramebuffer;

    public IvOpenGLTexturePingPong(Logger logger)
    {
        this.logger = logger;
    }

    public void initialize(boolean useFramebuffer)
    {
        destroy();
        this.useFramebuffer = useFramebuffer;

        boolean fboFailed = false;
        for (int i = 0; i < 2; i++)
        {
            cacheTextures[i] = IvOpenGLHelper.genStandardTexture();
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, screenWidth, screenHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        }

        if (cacheTextures[0] <= 0 || cacheTextures[1] <= 0)
        {
            fboFailed = true;
            setup = false;
        }
        else if (OpenGlHelper.framebufferSupported && useFramebuffer)
        {
            pingPongFB = glGenFramebuffersEXT();

            glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, pingPongFB);
            glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, GL_TEXTURE_2D, cacheTextures[0], 0);
            glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT1_EXT, GL_TEXTURE_2D, cacheTextures[1], 0);

            int status = glCheckFramebufferStatusEXT(GL_FRAMEBUFFER_EXT);
            if (status != GL_FRAMEBUFFER_COMPLETE_EXT)
            {
                logger.error("PingPong FBO failed setting up! (" + IvDepthBuffer.getFramebufferStatusString(status) + ")");

                fboFailed = true;
            }

            glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, parentFrameBuffer);

            setup = true;
        }
        else
        {
            fboFailed = true;
            setup = true;
        }

        if (!fboFailed)
        {
            setupRealtimeFB = true;
        }
        else
        {
            logger.error("Can not PingPong! Using screen pong workaround");
        }
    }

    public void setScreenSize(int screenWidth, int screenHeight)
    {
        boolean gen = screenWidth != this.screenWidth || screenHeight != this.screenHeight;

        if (gen)
        {
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;

            if (setup)
            {
                initialize(useFramebuffer);
            }
        }
    }

    public int getScreenWidth()
    {
        return screenWidth;
    }

    public int getScreenHeight()
    {
        return screenHeight;
    }

    public void setParentFrameBuffer(int parentFrameBuffer)
    {
        this.parentFrameBuffer = parentFrameBuffer > 0 ? parentFrameBuffer : 0;
    }

    public int getParentFrameBuffer()
    {
        return this.parentFrameBuffer;
    }

    public void preTick(int screenWidth, int screenHeight)
    {
        setupCacheTextureForTick = false;

        setScreenSize(screenWidth, screenHeight);
    }

    public void pingPong()
    {
        if (setupRealtimeFB)
        {
            if (!setupCacheTextureForTick)
            {
                activeBuffer = 0;
                bindCurrentTexture();

                glCopyTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, 0, 0, screenWidth, screenHeight, 0);

                glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, pingPongFB);
                glPushAttrib(GL_VIEWPORT_BIT | GL_COLOR_BUFFER_BIT);

                setupCacheTextureForTick = true;
            }
            else
            {
                activeBuffer = 1 - activeBuffer;
                bindCurrentTexture();
            }

            glDrawBuffer(activeBuffer == 1 ? GL_COLOR_ATTACHMENT0_EXT : GL_COLOR_ATTACHMENT1_EXT);
//            glReadBuffer(activeBuffer == 0 ? GL_COLOR_ATTACHMENT0_EXT : GL_COLOR_ATTACHMENT1_EXT);

            glViewport(0, 0, screenWidth, screenHeight);
//            glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
//            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//            IvOpenGLHelper.setUpOpenGLStandard2D(screenWidth, screenHeight);
        }
        else // Use direct draw workaround
        {
            glBindTexture(GL_TEXTURE_2D, cacheTextures[0]);
            glCopyTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, 0, 0, screenWidth, screenHeight, 0);
        }
    }

    public void bindCurrentTexture()
    {
        glBindTexture(GL_TEXTURE_2D, cacheTextures[activeBuffer]);
    }

    public void postTick()
    {
        if (setupRealtimeFB && setupCacheTextureForTick)
        {
//            glDrawBuffer(GL_BACK);
//            glReadBuffer(GL_BACK);
            glPopAttrib();
            glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, parentFrameBuffer);

            activeBuffer = 1 - activeBuffer;

            glColor3f(1.0f, 1.0f, 1.0f);
            bindCurrentTexture();
            IvRenderHelper.drawRectFullScreen(screenWidth, screenHeight);
        }
    }

    public void destroy()
    {
        for (int i = 0; i < 2; i++)
        {
            if (cacheTextures[i] > 0)
            {
                glDeleteTextures(cacheTextures[i]);
                cacheTextures[i] = 0;
            }
        }

        if (pingPongFB > 0)
        {
            glDeleteFramebuffersEXT(pingPongFB);
            pingPongFB = 0;
        }

        setupRealtimeFB = false;
        setup = false;
    }
}
