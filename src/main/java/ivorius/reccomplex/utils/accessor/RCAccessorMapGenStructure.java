/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.accessor;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Method;

/**
 * Created by lukas on 30.06.17.
 */
public class RCAccessorMapGenStructure
{
    private static Method getStructureAt;

    public static StructureStart getStructureAt(MapGenStructure gen, BlockPos pos)
    {
        if (getStructureAt == null)
            getStructureAt = ReflectionHelper.findMethod(MapGenStructure.class, "getStructureAt", "func_175797_c", BlockPos.class);

        return SafeReflector.invoke(gen, getStructureAt, null, pos);
    }
}
