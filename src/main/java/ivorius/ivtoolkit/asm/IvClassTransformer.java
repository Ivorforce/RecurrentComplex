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

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Created by lukas on 21.02.14.
 */
public abstract class IvClassTransformer
{
    public Logger logger;

    protected IvClassTransformer(Logger logger)
    {
        this.logger = logger;
    }

    public void printSubMethodError(String className, String methodID, String submethodID)
    {
        logger.error("Could not patch " + className + "." + methodID + " (" + submethodID + ")!");
    }

    public static String getMethodDescriptor(Object returnValue, Object... params)
    {
        Type[] types = new Type[params.length];

        for (int i = 0; i < params.length; i++)
        {
            types[i] = getType(params[i]);
        }

        return Type.getMethodDescriptor(getType(returnValue), types);
    }

    private static Type getType(Object obj)
    {
        if (obj instanceof Type)
        {
            return (Type) obj;
        }
        else if (obj instanceof Class)
        {
            return Type.getType((Class) obj);
        }
        else if (obj instanceof String)
        {
            return Type.getObjectType((String) obj);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    public static String getSRGDescriptor(String signature)
    {
        return FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(signature);
    }

    public static String getSrgName(MethodInsnNode node)
    {
        return IvDevRemapper.getSRGName(FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.owner, node.name, node.desc));
    }

    public static String getSrgName(String className, MethodNode node)
    {
        return IvDevRemapper.getSRGName(FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(className, node.name, node.desc));
    }

    public static String getSrgName(FieldInsnNode node)
    {
        return IvDevRemapper.getSRGName(FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(node.owner, node.name, node.desc));
    }

    public static String getSrgClassName(String className)
    {
        return FMLDeobfuscatingRemapper.INSTANCE.map(className);
    }

    public byte[] transform(String actualClassName, String srgClassName, byte[] data, boolean obf)
    {
        ClassNode classNode = null;
        boolean didChange = false;

        try
        {
            classNode = new ClassNode();
            ClassReader classReader = new ClassReader(data);
            classReader.accept(classNode, 0);
        }
        catch (Exception ex)
        {
            logger.error("Error patching class PRE " + actualClassName + " (" + srgClassName + ")!", ex);
            return data;
        }

        try
        {
            didChange = transform(actualClassName.replaceAll("\\.", "/"), classNode, obf);
        }
        catch (Exception ex)
        {
            logger.error("Error patching class MAIN " + actualClassName + " (" + srgClassName + ")!", ex);
            return data;
        }

        if (didChange)
        {
            try
            {
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
//            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES); // Compute Frames can crash sometimes...
                classNode.accept(writer);
                return writer.toByteArray();
            }
            catch (Exception ex)
            {
                logger.error("Error patching class POST " + actualClassName + " (" + srgClassName + ")!", ex);
                return data;
            }
        }

        return data;
    }

    public abstract boolean transform(String className, ClassNode classNode, boolean obf);
}
