/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.schematics.SchematicLoader;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static net.minecraft.command.CommandBase.getListOfStringsMatchingLastWord;

/**
 * Created by lukas on 31.05.17.
 */
public class RCExpect<T extends RCExpect<T>> extends IvExpect<T>
{
    RCExpect()
    {

    }

    public static <T extends RCExpect<T>> T startRC()
    {
        //noinspection unchecked
        return (T) new RCExpect();
    }

    public T structurePredicate()
    {
        return structure();
    }

    public T structure()
    {
        return next(StructureRegistry.INSTANCE.ids());
    }

    public T schematic() {
        return next(SchematicLoader.currentSchematicFileNames()
                .stream().map(name -> name.contains(" ") ? String.format("\"%s\"", name) : name).collect(Collectors.toList()));
    }

    public T rotation()
    {
        return any("0", "1", "2", "3");
    }

    public T resourceDirectory()
    {
        return any((Object[]) ResourceDirectory.values());
    }

    public T metadata()
    {
        return next(IntStream.range(0, 16).mapToObj(String::valueOf).collect(Collectors.toList()));
    }
}
