/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import ivorius.ivtoolkit.blocks.BlockSurfacePos;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.GenerationType;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.NaturalGeneration;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Created by lukas on 30.05.17.
 */
public class Parameter
{
    private int moved;
    private final String name;
    private final List<String> params;

    public Parameter(String name, List<String> params)
    {
        this.name = name;
        this.params = params;
    }

    private Parameter(int moved, String name, List<String> params)
    {
        this.moved = moved;
        this.name = name;
        this.params = params;
    }

    public Parameter move(int idx)
    {
        return new Parameter(moved + idx, name, params.subList(idx, params.size()));
    }

    public Result<String> here()
    {
        return at(0);
    }

    @Nonnull
    public Result<BlockSurfacePos> surfacePos(ICommandSender sender, boolean centerBlock)
    {
        return at(0).failable().flatMap(x -> at(1).map(z ->
                RCCommands.surfacePos(sender.getPosition(), x, z, centerBlock)))
                .orElse(() -> BlockSurfacePos.from(sender.getPosition()));
    }

    public Result<WorldServer> dimension(ICommandSender commandSender)
    {
        return at(0).filter(d -> !d.equals("~")).failable()
                .map(CommandBase::parseInt).map(DimensionManager::getWorld, () -> ServerTranslations.commandException("commands.rc.nodimension"))
                .orElse(() -> (WorldServer) commandSender.getEntityWorld());
    }

    public Result<Structure<?>> structure()
    {
        return at(0).map(StructureRegistry.INSTANCE::get,
                () -> ServerTranslations.commandException("commands.strucGen.noStructure", at(0)));
    }

    public Result<GenerationType> generationType(Structure<?> structure)
    {
        return at(0).failable().map(structure::generationType, () -> ServerTranslations.commandException("No Generation by this ID"))
                .orElse(() -> structure.<GenerationType>generationTypes(NaturalGeneration.class).stream().findFirst()
                        .orElse(structure.generationTypes(GenerationType.class).stream().findFirst().orElse(null)));
    }

    public boolean has(int size)
    {
        return size <= params.size();
    }

    public Result<String> at(int index)
    {
        return new Result<>(() ->
        {
            if (!has(index + 1))
                throw new CommandException(String.format("Missing required parameter: -%s (%d)", name, index + moved));

            return params.get(index);
        });
    }

    protected interface Supplier<T>
    {
        T get() throws CommandException;
    }

    protected interface Function<T, O>
    {
        O apply(T t) throws CommandException;
    }

    public class Result<T>
    {
        private Supplier<T> t;

        public Result(Supplier<T> t)
        {
            this.t = t;
        }

        public Result<T> filter(Predicate<T> fun)
        {
            return new Result<T>(() ->
            {
                T t = this.t.get();
                if (!fun.test(t)) throw new CommandException("Parameter not accepted!");
                return t;
            });
        }

        public <O> Result<O> map(Function<T, O> fun)
        {
            return map(fun, null);
        }

        public <O> Result<O> map(Function<T, O> fun, @Nullable Supplier<CommandException> exc)
        {
            return new Result<>(() ->
            {
                T t = this.t.get();

                if (t == null) return null;

                O o = fun.apply(t);
                if (o == null && exc != null) throw exc.get();

                return o;
            });
        }

        public <O> Result<O> flatMap(Function<T, Result<O>> fun)
        {
            return new Result<>(() ->
            {
                T t = this.t.get();

                if (t == null) return null;

                return fun.apply(t).t.get();
            });
        }

        public Result<T> orElse(Supplier<T> supplier)
        {
            return new Result<T>(() ->
            {
                T t = this.t.get();
                return t != null ? t : supplier.get();
            });
        }

        public Result<T> failable()
        {
            return new Result<T>(() ->
            {
                try
                {
                    return t.get();
                }
                catch (CommandException e)
                {
                    return null;
                }
            });
        }

        @Nonnull
        public T require() throws CommandException
        {
            T t = this.t.get();
            if (t == null) throw new CommandException("Parameter missing!");
            return t;
        }

        public Optional<T> optional()
        {
            T t = null;

            try
            {
                t = this.t.get();
            }
            catch (CommandException ignored)
            {
            }

            return Optional.ofNullable(t);
        }

        @Override
        public String toString()
        {
            return optional().map(Object::toString).orElse("null");
        }
    }
}
