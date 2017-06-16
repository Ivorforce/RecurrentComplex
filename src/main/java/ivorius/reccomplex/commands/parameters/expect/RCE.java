/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters.expect;

import ivorius.reccomplex.commands.CommandVirtual;
import ivorius.mcopts.commands.parameters.expect.Expect;
import ivorius.mcopts.commands.parameters.Parameter;
import ivorius.mcopts.commands.parameters.Parameters;
import ivorius.reccomplex.commands.parameters.RCP;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.random.Person;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.GenerationType;
import ivorius.reccomplex.world.gen.feature.structure.schematics.SchematicLoader;
import net.minecraft.util.EnumFacing;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by lukas on 31.05.17.
 */
public class RCE
{
    public static void structurePredicate(Expect e)
    {
        e.then(RCE::structure).descriptionU("resource expression");
    }

    public static void structure(Expect e)
    {
        e.next(StructureRegistry.INSTANCE.ids()).descriptionU("structure");
    }

    public static Consumer<Expect> generationType(Function<Parameters, Parameter<String>> fun)
    {
        return e -> e.next(params -> fun.apply(params).to(RCP::structure).tryGet()
                .map(structure -> structure.generationTypes(GenerationType.class).stream().map(GenerationType::id)))
                .descriptionU("generation type id");
    }

    public static void schematic(Expect e)
    {
        e.next(SchematicLoader.currentSchematicFileNames()).descriptionU("schematic");
    }

    public static void resourceDirectory(Expect e)
    {
        e.any((Object[]) ResourceDirectory.values()).descriptionU("directory");
    }

    public static void metadata(Expect e)
    {
        e.next(IntStream.range(0, 16).mapToObj(String::valueOf).collect(Collectors.toList())).descriptionU("metadata");
    }

    public static void virtualCommand(Expect ex)
    {
        ex.next((server, sender, args, pos) -> server.getCommandManager().getCommands().entrySet().stream()
                .filter(e -> e.getValue() instanceof CommandVirtual).map(Map.Entry::getKey).collect(Collectors.toList()));
        ex.descriptionU("virtual command");
    }

    public static void directionExpression(Expect ex)
    {
        List<String> ret = new ArrayList<>();
        ret.addAll(Arrays.stream(EnumFacing.values()).map(EnumFacing::getName2).collect(Collectors.toList()));
        ret.addAll(Arrays.stream(EnumFacing.Axis.values()).map(EnumFacing.Axis::getName).collect(Collectors.toList()));
        Collections.addAll(ret, "horizontal", "vertical");

        ex.next(ret).descriptionU("direction expression");
    }

    public static void randomString(Expect e)
    {
        Random rand = new Random();
        e.any(Person.chaoticName(rand, rand.nextBoolean()));
    }
}
