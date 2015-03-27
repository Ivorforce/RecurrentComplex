/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.villages;

import ivorius.reccomplex.RecurrentComplex;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.CheckClassAdapter;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.objectweb.asm.Opcodes.*;

/**
 * Created by lukas on 26.03.15.
 */
public class VanillaGenerationClassFactory extends ClassLoader
{
    private static final VanillaGenerationClassFactory INSTANCE = new VanillaGenerationClassFactory();

    protected Map<String, Class<?>> loaded = new HashMap<>();
    protected Set<String> failed = new HashSet<>();

    public VanillaGenerationClassFactory()
    {
        super(VanillaGenerationClassFactory.class.getClassLoader());
    }

    public static VanillaGenerationClassFactory instance()
    {
        return INSTANCE;
    }

    @Nullable
    public GenericVillagePiece create(String structureID, String generationID)
    {
        Class<? extends GenericVillagePiece> aClass = getClass(structureID, generationID);

        try
        {
            return aClass != null ? aClass.newInstance() : null;
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    @Nullable
    public GenericVillagePiece create(String structureID, String generationID, StructureVillagePieces.Start start, int generationDepth)
    {
        Class<? extends GenericVillagePiece> aClass = getClass(structureID, generationID);

        if (aClass != null)
        {
            try
            {
                Constructor<? extends GenericVillagePiece> constructor;
                constructor = aClass.getConstructor(StructureVillagePieces.Start.class, Integer.TYPE);
                return constructor.newInstance(start, generationDepth);
            }
            catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e)
            {
                e.printStackTrace();
            }
        }

        return null;
    }

    public Class<? extends GenericVillagePiece> getClass(String structureID, String generationID)
    {
        try
        {
            return (Class<? extends GenericVillagePiece>) findClass(classNameForStructure(structureID, generationID));
        }
        catch (ClassNotFoundException ignored)
        {
        }

        if (!failed.contains(structureID))
        {
            Class<? extends GenericVillagePiece> created = createClass(structureID, generationID);
            if (created == null)
            {
                failed.add(structureID);
                RecurrentComplex.logger.error("Could not create vanilla generation type class for '" + structureID + "'");
            }
            return created;
        }

        return null;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException
    {
        if (loaded.containsKey(name))
            return loaded.get(name);

        return super.findClass(name);
    }

    protected Class<?> define(String name, byte[] data)
    {
        Class<?> aClass = defineClass(name, data, 0, data.length);
        loaded.put(name, aClass);
        return aClass;
    }

    protected Class<? extends GenericVillagePiece> createClass(String structureID, String generationID)
    {
        try
        {
            String className = classNameForStructure(structureID, generationID);
            return (Class<? extends GenericVillagePiece>) define(className, createClassBinary(className.replaceAll("\\.", "/")));
        }
        catch (Throwable t)
        {
            RecurrentComplex.logger.error("Can't load dynamic piece class", t);
            return null;
        }
    }

    protected String classNameForStructure(String structureID, String generationID)
    {
        return "ivorius.reccomplex.dynamic.vanillagen." + structureID + "_" + generationID;
    }

    protected byte[] createClassBinary(String className)
    {
        ClassWriter writer = new ClassWriter(0);
        ClassVisitor cw = new CheckClassAdapter(writer);
        cw.visit(V1_5, ACC_PUBLIC, className, null, Type.getInternalName(GenericVillagePiece.class), null);

        {
            // empty constructor
            String descriptor = Type.getMethodDescriptor(Type.VOID_TYPE);
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", descriptor, null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(GenericVillagePiece.class), "<init>", descriptor, false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 1);
            mv.visitEnd();
        }

        {
            // initial constructor
            String descriptor = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(StructureVillagePieces.Start.class), Type.INT_TYPE);
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", descriptor, null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ILOAD, 2);
            mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(GenericVillagePiece.class), "<init>", descriptor, false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(4, 3);
            mv.visitEnd();
        }

        cw.visitEnd();

        return writer.toByteArray();
    }
}
