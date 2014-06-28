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

import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;

/**
 * Created by lukas on 12.03.14.
 */
public abstract class IvClassTransformerClass extends IvClassTransformer
{
    public ArrayList<String[]> registeredMethods;

    public IvClassTransformerClass(Logger logger)
    {
        super(logger);
        registeredMethods = new ArrayList<String[]>();
    }

    public void registerExpectedMethod(String methodID, String obfName, String signature)
    {
        registeredMethods.add(new String[]{obfName, signature, methodID});
    }

    @Override
    public boolean transform(String className, ClassNode classNode, boolean obf)
    {
        boolean[] sigs = new boolean[registeredMethods.size()];

        for (MethodNode m : classNode.methods)
        {
            for (int methodIndex = 0; methodIndex < registeredMethods.size(); methodIndex++)
            {
                String[] methodInfo = registeredMethods.get(methodIndex);
                String srgName = getSrgName(className, m);
                String srgSignature = getSRGDescriptor(m.desc);

                if ((srgName.equals(methodInfo[0]) && srgSignature.equals(methodInfo[1])))
                {
                    if (transformMethod(className, methodInfo[2], m, obf))
                    {
                        sigs[methodIndex] = true;
                    }
                }
            }
        }

        boolean didChange = false;

        for (int methodIndex = 0; methodIndex < registeredMethods.size(); methodIndex++)
        {
            if (!sigs[methodIndex])
            {
                String[] methodInfo = registeredMethods.get(methodIndex);

                logger.error("Could not transform expected method in class \"" + className + "\" (Obf: " + obf + "): " + methodInfo[0] + " - " + methodInfo[1] + " - " + methodInfo[2]);
            }
            else
            {
                didChange = true;
            }
        }

        return didChange;
    }

    public abstract boolean transformMethod(String className, String methodID, MethodNode methodNode, boolean obf);
}
