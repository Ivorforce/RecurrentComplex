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

package ivorius.ivtoolkit.rendering.textures;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Created by lukas on 27.07.14.
 */
public class PreBufferedTexture extends AbstractTexture
{
    private BufferedImage bufferedImage;

    public PreBufferedTexture(BufferedImage bufferedImage)
    {
        this.bufferedImage = bufferedImage;
    }

    public BufferedImage getBufferedImage()
    {
        return bufferedImage;
    }

    @Override
    public void loadTexture(IResourceManager var1) throws IOException
    {
        TextureUtil.uploadTextureImageAllocate(this.getGlTextureId(), bufferedImage, false, false);
    }
}
