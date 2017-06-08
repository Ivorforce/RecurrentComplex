/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.rcparameters;

import ivorius.reccomplex.commands.CommandVirtual;
import ivorius.reccomplex.commands.parameters.Expect;
import ivorius.reccomplex.commands.parameters.Parameter;
import ivorius.reccomplex.commands.parameters.Parameters;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.GenerationType;
import ivorius.reccomplex.world.gen.feature.structure.schematics.SchematicLoader;
import net.minecraft.util.EnumFacing;

import java.util.*;
import java.util.function.Function;
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
        return structure().descriptionU("resource expression");
    }

    public T structure()
    {
        return next(StructureRegistry.INSTANCE.ids()).descriptionU("structure");
    }

    public T generationType(Function<Parameters, Parameter<String>> fun)
    {
        return next(params -> fun.apply(params).to(RCP::structure).tryGet()
                .map(structure -> structure.generationTypes(GenerationType.class).stream().map(GenerationType::id)));
    }

    public T schematic()
    {
        return next(SchematicLoader.currentSchematicFileNames()
                .stream().map(name -> name.contains(" ") ? String.format("\"%s\"", name) : name).collect(Collectors.toList())).descriptionU("schematic");
    }

    public T resourceDirectory()
    {
        return any((Object[]) ResourceDirectory.values()).descriptionU("directory");
    }

    public T metadata()
    {
        return next(IntStream.range(0, 16).mapToObj(String::valueOf).collect(Collectors.toList())).descriptionU("metadata");
    }

    public T virtualCommand()
    {
        Expect<T> tExpect = next((server, sender, args, pos) -> server.getCommandManager().getCommands().entrySet().stream()
                .filter(e -> e.getValue() instanceof CommandVirtual).map(Map.Entry::getKey).collect(Collectors.toList()));
        return tExpect.descriptionU("virtual command");
    }

    public T directionExpression()
    {
        List<String> ret = new ArrayList<>();
        ret.addAll(Arrays.stream(EnumFacing.values()).map(EnumFacing::getName2).collect(Collectors.toList()));
        ret.addAll(Arrays.stream(EnumFacing.Axis.values()).map(EnumFacing.Axis::getName).collect(Collectors.toList()));
        Collections.addAll(ret, "horizontal", "vertical");

        return next(ret).descriptionU("direction expression");
    }
}
