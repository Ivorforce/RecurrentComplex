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
import net.minecraft.server.MinecraftServer;

import java.util.function.BinaryOperator;
import java.util.function.Predicate;

/**
 * Created by lukas on 31.05.17.
 */
public class RCParameter<P extends RCParameter<P>> extends IvParameter<P>
{
    public RCParameter(Parameter other)
    {
        super(other);
    }

    public Parameter<Predicate<Structure>, ?> structurePredicate()
    {
        return expression(new ResourceExpression(s1 -> !s1.isEmpty())).map(m -> s -> m.test(StructureRegistry.INSTANCE.resourceLocation(s)));
    }

    @Override
    public P copy(Parameter<String, ?> p)
    {
        //noinspection unchecked
        return (P) new RCParameter<>(p);
    }

    public Parameter<Structure<?>, ?> structure()
    {
        //noinspection unchecked
        return ((Parameter<String, ?>) this).map(StructureRegistry.INSTANCE::get,
                t -> ServerTranslations.commandException("commands.strucGen.noStructure", get()));
    }

    public Parameter<GenericStructure, ?> genericStructure()
    {
        return map(id ->
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

    public Parameter<GenerationType, ?> generationType(Structure<?> structure)
    {
        return map(structure::generationType, t -> ServerTranslations.commandException("No Generation by this ID"))
                .orElseGet(() -> structure.<GenerationType>generationTypes(NaturalGeneration.class).stream().findFirst()
                        .orElse(structure.generationTypes(GenerationType.class).stream().findFirst().orElse(null)));
    }

    public Parameter<ResourceDirectory, ?> resourceDirectory()
    {
        return map(t ->
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

    public Parameter<int[], ?> metadatas()
    {
        return map(arg ->
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
        });
    }

    public <T extends ExpressionCache<I>, I> Parameter<T, ?> expression(T t)
    {
        return map(s ->
        {
            T cache = ExpressionCache.of(t, s);
            RCCommands.ensureValid(cache, name);
            return cache;
        });
    }

    public Parameter<CommandVirtual, ?> virtualCommand(MinecraftServer server)
    {
        return new MCParameter(this).command(server).map(c ->
        {
            if (!(c instanceof CommandVirtual))
                throw ServerTranslations.commandException("commands.rcmap.nonvirtual");
            return (CommandVirtual) c;
        });
    }
}
