/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import ivorius.reccomplex.utils.accessor.RCAccessorBiomeDictionary;
import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by lukas on 31.05.17.
 */
public class MCExpect<T extends MCExpect<T>> extends Expect<T>
{
    MCExpect()
    {

    }

    public static <T extends MCExpect<T>> T expectMC()
    {
        //noinspection unchecked
        return (T) new MCExpect();
    }

    public T xyz()
    {
        return x().y().z();
    }

    public T pos(String x, String y, String z)
    {
        return named(x).x()
                .named(y).y()
                .named(z).z();
    }

    public T x()
    {
        return next((ser, sen, args, pos) -> CommandBase.getTabCompletionCoordinate(args.lastAsArray(), 0, pos))
                .optionalU("x");
    }

    public T y()
    {
        return next((ser, sen, args, pos) -> CommandBase.getTabCompletionCoordinate(args.lastAsArray(), -1, pos))
                .optionalU("y");
    }

    public T z()
    {
        return next((ser, sen, args, pos) -> CommandBase.getTabCompletionCoordinate(args.lastAsArray(), -2, pos))
                .optionalU("z");
    }

    public T biome()
    {
        return next(Biome.REGISTRY.getKeys())
                .optionalU("biome");
    }

    public T biomeType()
    {
        return next(RCAccessorBiomeDictionary.getMap().keySet()).optionalU("biome type");
    }

    public T dimension()
    {
        return next(Arrays.stream(DimensionManager.getIDs()))
                .optionalU("dimension");
    }

    public T block()
    {
        return next(Block.REGISTRY.getKeys())
                .optionalU("block");
    }

    public T command()
    {
        return next((server, sender, args, pos) -> server.getCommandManager().getCommands().keySet())
                .optionalU("command");
    }

    public T commandArguments(Parameter parameter, ICommandSender sender)
    {
        return next((server1, sender1, args1, pos1) ->
        {
            Optional<ICommand> other = parameter.first().tryGet().map(server1.getCommandManager().getCommands()::get);
            return other.map(c -> c.getTabCompletions(server1, sender, parameter.move(1).varargs(), pos1)).orElse(Collections.emptyList());
        })
                .optionalU("args...");
    }

    public T entity(MinecraftServer server)
    {
        return any((Object[]) server.getOnlinePlayerNames())
                .optionalU("entity");
    }
}
