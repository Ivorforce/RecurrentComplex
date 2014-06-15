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

package ivorius.structuregen.ivtoolkit;

import com.google.common.base.Charsets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * Created by lukas on 17.02.14.
 */
public class IvShaderInstanceMC
{
    public static void trySettingUpShader(IvShaderInstance shaderInstance, ResourceLocation vertexShader, ResourceLocation fragmentShader, String utils)
    {
        String vShader = null;
        String fShader = null;

        IResource vShaderRes = null;
        IResource fShaderRes = null;

        try
        {
            vShaderRes = Minecraft.getMinecraft().getResourceManager().getResource(vertexShader);
            fShaderRes = Minecraft.getMinecraft().getResourceManager().getResource(fragmentShader);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (vShaderRes != null && fShaderRes != null)
        {
            try
            {
                vShader = IOUtils.toString(vShaderRes.getInputStream(), Charsets.UTF_8);
                fShader = IOUtils.toString(fShaderRes.getInputStream(), Charsets.UTF_8);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            if (vShader != null && fShader != null)
            {
                if (utils != null)
                {
                    vShader = addUtils(vShader, utils);
                    fShader = addUtils(fShader, utils);
                }

                shaderInstance.trySettingUpShader(vShader, fShader);
            }
        }
    }

    public static String addUtils(String shader, String utils)
    {
        int indexVersion = shader.indexOf("#version");
        if (indexVersion < 0)
        {
            indexVersion = 0;
        }

        int indexVersionNL = shader.indexOf("\n", indexVersion);

        return shader.substring(0, indexVersionNL + 1) + utils + shader.substring(indexVersionNL + 1);
    }
}
