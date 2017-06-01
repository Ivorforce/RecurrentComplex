/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import ivorius.reccomplex.commands.CommandVirtual;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.utils.algebra.ExpressionCache;
import ivorius.reccomplex.utils.expression.ResourceExpression;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.GenerationType;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.NaturalGeneration;
import net.minecraft.command.CommandException;
import net.minecraft.server.MinecraftServer;

import java.util.function.Predicate;

/**
 * Created by lukas on 31.05.17.
 */
public class RCParameter extends Parameter
{
    public RCParameter(Parameter other)
    {
        super(other);
    }

    public static int[] parseMetadatas(String arg) throws CommandException
    {
        try
        {
            String[] strings = arg.split(",");
            int[] ints = new int[strings.length];

            for (int i = 0; i < strings.length; i++)
            {
                ints[i] = Integer.valueOf(strings[i]);
            }

            return ints;
        }
        catch (Exception ex)
        {
            throw ServerTranslations.wrongUsageException("commands.selectModify.invalidMetadata", arg);
        }
    }

    public Result<Predicate<Structure>> structurePredicate()
    {
        return expression(new ResourceExpression(s1 -> !s1.isEmpty())).map(m -> s -> m.test(StructureRegistry.INSTANCE.resourceLocation(s)));
    }

    @Override
    public RCParameter move(int idx)
    {
        return new RCParameter(super.move(idx));
    }

    public Result<Structure<?>> structure()
    {
        return first().map(StructureRegistry.INSTANCE::get,
                t -> ServerTranslations.commandException("commands.strucGen.noStructure", first()));
    }

    public Result<GenericStructure> genericStructure()
    {
        return first().map(id ->
        {
            Structure structure = StructureRegistry.INSTANCE.get(id);

            if (structure == null)
                throw ServerTranslations.commandException("commands.structure.notRegistered", id);

            GenericStructure genericStructureInfo = structure.copyAsGenericStructure();

            if (genericStructureInfo == null)
                throw ServerTranslations.commandException("commands.structure.notGeneric", id);

            return genericStructureInfo;
        });
    }

    public Result<GenerationType> generationType(Structure<?> structure)
    {
        return first().missable().map(structure::generationType, t -> ServerTranslations.commandException("No Generation by this ID"))
                .orElseGet(() -> structure.<GenerationType>generationTypes(NaturalGeneration.class).stream().findFirst()
                        .orElse(structure.generationTypes(GenerationType.class).stream().findFirst().orElse(null)));
    }

    public Result<ResourceDirectory> resourceDirectory()
    {
        return first().map(t ->
        {
            try
            {
                return ResourceDirectory.valueOf(t);
            }
            catch (IllegalArgumentException e)
            {
                throw ServerTranslations.commandException("commands.rcsave.nodirectory");
            }
        });
    }

    public Result<int[]> metadatas()
    {
        return first().map(RCParameter::parseMetadatas);
    }

    public <T extends ExpressionCache<I>, I> Result<T> expression(T t)
    {
        return text().map(s ->
        {
            T cache = ExpressionCache.of(t, s);
            RCCommands.ensureValid(cache, name);
            return cache;
        });
    }

    public Result<CommandVirtual> virtualCommand(MinecraftServer server)
    {
        return new MCParameter(this).command(server).map(c ->
        {
            if (!(c instanceof CommandVirtual))
                throw ServerTranslations.commandException("commands.rcmap.nonvirtual");
            return (CommandVirtual) c;
        });
    }
}
