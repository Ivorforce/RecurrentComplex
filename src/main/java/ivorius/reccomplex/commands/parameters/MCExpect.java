/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import ivorius.reccomplex.utils.accessor.RCAccessorBiomeDictionary;
import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

/**
 * Created by lukas on 31.05.17.
 */
public class MCExpect<T extends MCExpect<T>> extends Expect<T>
{
    protected MCExpect()
    {

    }

    public static <T extends MCExpect<T>> T expectMC()
    {
        //noinspection unchecked
        return (T) new MCExpect();
    }

    public T xyz()
    {
        return x().y().z().atOnce(3);
    }

    public T pos(String x, String y, String z)
    {
        return named(x).x()
                .named(y).y()
                .named(z).z()
                .atOnce(3);
    }

    public T x()
    {
        Expect<T> tExpect = nextRaw((ser, sen, args, pos) -> CommandBase.getTabCompletionCoordinate(args.lastAsArray(), 0, pos));
        return tExpect.descriptionU("x");
    }

    public T y()
    {
        Expect<T> tExpect = nextRaw((ser, sen, args, pos) -> CommandBase.getTabCompletionCoordinate(args.lastAsArray(), -1, pos));
        return tExpect.descriptionU("y");
    }

    public T z()
    {
        Expect<T> tExpect = nextRaw((ser, sen, args, pos) -> CommandBase.getTabCompletionCoordinate(args.lastAsArray(), -2, pos));
        return tExpect.descriptionU("z");
    }

    public T biome()
    {
        return next(Biome.REGISTRY.getKeys()).descriptionU("biome");
    }

    public T biomeType()
    {
        return next(RCAccessorBiomeDictionary.getMap().keySet()).descriptionU("biome type");
    }

    public T dimension()
    {
        return next(Arrays.stream(DimensionManager.getIDs())).descriptionU("dimension");
    }

    public T block()
    {
        return next(Block.REGISTRY.getKeys()).descriptionU("block");
    }

    public T command()
    {
        Expect<T> tExpect = next((server, sender, args, pos) -> server.getCommandManager().getCommands().keySet());
        return tExpect.descriptionU("command");
    }

    public T commandArguments(Function<Parameters, Parameter<String>> start)
    {
        Expect<T> tExpect = nextRaw((server1, sender, params, pos1) ->
        {
            Parameter<String> commandParameter = start.apply(params);
            Optional<ICommand> other = commandParameter.tryGet().map(server1.getCommandManager().getCommands()::get);
            return other.map(c -> c.getTabCompletions(server1, sender, commandParameter.move(1).to(NaP::varargs).get(), pos1)).orElse(Collections.emptyList());
        });
        return tExpect.descriptionU("args...");
    }

    public T entity()
    {
        Expect<T> tExpect = next((server, sender, parameters, pos) -> Arrays.stream(server.getOnlinePlayerNames()));
        return tExpect.descriptionU("entity");
    }

    public T rotation()
    {
        return any("0", "90", "180", "270").descriptionU("rotation");
    }
}
