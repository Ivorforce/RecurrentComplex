/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import ivorius.mcopts.commands.parameters.MCP;
import ivorius.mcopts.commands.parameters.Parameter;
import ivorius.mcopts.commands.parameters.Parameters;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.commands.CommandVirtual;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.utils.algebra.ExpressionCache;
import ivorius.reccomplex.utils.expression.Expressions;
import ivorius.reccomplex.utils.expression.ResourceExpression;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.GenerationType;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.NaturalGeneration;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by lukas on 31.05.17.
 */
public class RCP
{
    public static Parameter<Structure<?>> structure(Parameter<String> p)
    {
        return p.map(StructureRegistry.INSTANCE::get,
                t -> RecurrentComplex.translations.commandException("commands.strucGen.noStructure", p.get()));
    }

    public static Parameter<GenericStructure> genericStructure(Parameter<String> p, boolean copy)
    {
        return p.map(id ->
        {
            Structure structure = StructureRegistry.INSTANCE.get(id);

            if (structure == null)
                throw RecurrentComplex.translations.commandException("commands.structure.notRegistered", id);

            if (copy)
                structure = structure.copyAsGenericStructure();

            if (!(structure instanceof GenericStructure))
                throw RecurrentComplex.translations.commandException("commands.structure.notGeneric", id);

            return (GenericStructure) structure;
        });
    }

    public static Parameter<GenericStructure> structureFromBlueprint(Parameter<String> p, ICommandSender sender)
    {
        return genericStructure(p, true).map(GenericStructure::copyAsGenericStructure)
                .orElseGet(() ->
                {
                    GenericStructure structure = GenericStructure.createDefaultStructure();
                    structure.metadata.authors = sender.getName();
                    return structure;
                });
    }

    public static Parameter<GenerationType> generationType(Parameter<String> p, Structure<?> structure)
    {
        return p.map(structure::generationType, t -> RecurrentComplex.translations.commandException("No Generation by this ID"))
                .orElseGet(() -> structure.<GenerationType>generationTypes(NaturalGeneration.class).stream().findFirst()
                        .orElse(structure.generationTypes(GenerationType.class).stream().findFirst().orElse(null)));
    }

    public static Parameter<Predicate<Structure>> structurePredicate(Parameter<String> p)
    {
        return p.to(RCP::expression, new ResourceExpression(s1 -> !s1.isEmpty())).map(m -> s -> m.test(StructureRegistry.INSTANCE.resourceLocation(s)));
    }

    public static Parameter<ResourceDirectory> resourceDirectory(Parameter<String> p)
    {
        return p.map(t ->
        {
            try
            {
                return ResourceDirectory.valueOf(t);
            }
            catch (IllegalArgumentException e)
            {
                throw RecurrentComplex.translations.commandException("commands.rcsave.nodirectory");
            }
        });
    }

    public static Parameter<int[]> metadatas(Parameter<String> p)
    {
        return p.map(arg ->
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
                throw RecurrentComplex.translations.wrongUsageException("commands.selectModify.invalidMetadata", arg);
            }
        });
    }

    public static <T extends ExpressionCache<I>, I> Parameter<T> expression(Parameter<String> p, T t)
    {
        return p.map(s ->
        {
            T cache = ExpressionCache.of(t, s);
            RCCommands.ensureValid(cache, p.name(0));
            return cache;
        });
    }

    public static Parameter<CommandVirtual> virtualCommand(Parameter<String> p, MinecraftServer server)
    {
        return p.to(MCP::command, server).map(c ->
        {
            if (!(c instanceof CommandVirtual))
                throw RecurrentComplex.translations.commandException("commands.rcmap.nonvirtual");
            return (CommandVirtual) c;
        });
    }

    public static Parameter<List<EnumFacing>> directions(Parameter<String> parameter)
    {
        return parameter.to(RCP::expression, Expressions.direction()).map(Expressions::directions);
    }

    public static Parameter<Shape> shape(Parameter<String> parameter)
    {
        return parameter.map(Shape::valueOf, s -> new CommandException("Invalid Shape!"));
    }

    public static BlockPos parseBlockPos(BlockPos blockpos, String[] args, int startIndex, boolean centerBlock) throws NumberInvalidException
    {
        return new BlockPos(CommandBase.parseDouble((double) blockpos.getX(), args[startIndex], -30000000, 30000000, centerBlock), CommandBase.parseDouble((double) blockpos.getY(), args[startIndex + 1], -30000000, 30000000, false), CommandBase.parseDouble((double) blockpos.getZ(), args[startIndex + 2], -30000000, 30000000, centerBlock));
    }

    public static Function<Parameter<String>, Parameter<BlockPos>> pos(Parameter<String> yp, Parameter<String> zp, BlockPos ref, boolean centerBlock)
    {
        return xp -> xp.orElse("~").flatMap(x ->
                yp.orElse("~").flatMap(y ->
                        zp.orElse("~").map(z ->
                                parseBlockPos(ref, new String[]{x, y, z}, 0, centerBlock)
                        )));
    }

    public static Function<Parameters, Parameter<BlockPos>> pos(String x, String y, String z, BlockPos ref, boolean centerBlock)
    {
        return p -> p.get(x).to(pos(p.get(y), p.get(z), ref, centerBlock));
    }

    public enum Shape
    {
        cube, sphere
    }
}
