/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import ivorius.reccomplex.commands.CommandVirtual;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.schematics.SchematicLoader;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by lukas on 31.05.17.
 */
public class RCExpect<T extends RCExpect<T>> extends IvExpect<T>
{
    RCExpect()
    {

    }

    public static <T extends RCExpect<T>> T expectRC()
    {
        //noinspection unchecked
        return (T) new RCExpect();
    }

    public T structurePredicate()
    {
        return structure()
                .optionalU("structure predicate");
    }

    public T structure()
    {
        return next(StructureRegistry.INSTANCE.ids())
                .optionalU("structure");
    }

    public T schematic()
    {
        return next(SchematicLoader.currentSchematicFileNames()
                .stream().map(name -> name.contains(" ") ? String.format("\"%s\"", name) : name).collect(Collectors.toList()))
                .optionalU("schematic");
    }

    public T rotation()
    {
        return any("0", "1", "2", "3").optionalU("rotation");
    }

    public T resourceDirectory()
    {
        return any((Object[]) ResourceDirectory.values()).optionalU("directory");
    }

    public T metadata()
    {
        return next(IntStream.range(0, 16).mapToObj(String::valueOf).collect(Collectors.toList()))
                .optionalU("metadata");
    }

    public T virtualCommand()
    {
        return next((server, sender, args, pos) -> server.getCommandManager().getCommands().entrySet().stream()
                .filter(e -> e.getValue() instanceof CommandVirtual).map(Map.Entry::getKey).collect(Collectors.toList()))
                .optionalU("virtual command");
    }
}
