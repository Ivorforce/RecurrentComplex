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

package ivorius.ivtoolkit.asm;

import net.minecraft.launchwrapper.IClassTransformer;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by lukas on 21.02.14.
 */
public class IvClassTransformerManager implements IClassTransformer
{
    public Hashtable<String, IvClassTransformer> transformers;
    public ArrayList<IvClassTransformer> generalTransformers;

    public IvClassTransformerManager()
    {
        transformers = new Hashtable<String, IvClassTransformer>();
        generalTransformers = new ArrayList<IvClassTransformer>();

        IvDevRemapper.setUp();
    }

    public void registerTransformer(String clazz, IvClassTransformer transformer)
    {
        transformers.put(clazz, transformer);
    }

    public void registerTransformer(IvClassTransformer transformer)
    {
        generalTransformers.add(transformer);
    }

    @Override
    public byte[] transform(String arg0, String arg1, byte[] arg2)
    {
        if (arg2 != null)
        {
            byte[] result = arg2;

            IvClassTransformer transformer = transformers.get(arg1);
            if (transformer != null)
            {
                byte[] data = transformer.transform(arg0, arg1, result, arg0.equals(arg1));

                if (data != null)
                {
                    result = data;
                }
            }

            for (IvClassTransformer generalTransformer : generalTransformers)
            {
                byte[] data = generalTransformer.transform(arg0, arg1, result, arg0.equals(arg1));

                if (data != null)
                {
                    result = data;
                }
            }

            return result;
        }

        return arg2;
    }
}
