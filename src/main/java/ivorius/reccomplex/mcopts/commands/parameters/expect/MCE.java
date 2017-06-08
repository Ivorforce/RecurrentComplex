/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.mcopts.commands.parameters.expect;

import ivorius.reccomplex.mcopts.accessor.AccessorBiomeDictionary;
import ivorius.reccomplex.mcopts.commands.parameters.NaP;
import ivorius.reccomplex.mcopts.commands.parameters.Parameter;
import ivorius.reccomplex.mcopts.commands.parameters.Parameters;
import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by lukas on 31.05.17.
 */
public class MCE
{
    public static void xyz(Expect e)
    {
        e.then(MCE::x).then(MCE::y).then(MCE::z).atOnce(3);
    }

    public static void xz(Expect e)
    {
        e.then(MCE::x).then(MCE::z).atOnce(3);
    }

    public static Consumer<Expect> pos(String x, String y, String z)
    {
        return e -> e.named(x).then(MCE::x)
                .named(y).then(MCE::y)
                .named(z).then(MCE::z)
                .atOnce(3);
    }

    public static void x(Expect e)
    {
        e.nextRaw((ser, sen, args, pos) -> CommandBase.getTabCompletionCoordinate(args.lastAsArray(), 0, pos))
                .descriptionU("x");
    }

    public static void y(Expect e)
    {
        e.nextRaw((ser, sen, args, pos) -> CommandBase.getTabCompletionCoordinate(args.lastAsArray(), -1, pos))
                .descriptionU("y");
    }

    public static void z(Expect e)
    {
        e.nextRaw((ser, sen, args, pos) -> CommandBase.getTabCompletionCoordinate(args.lastAsArray(), -2, pos))
                .descriptionU("z");
    }

    public static void biome(Expect e)
    {
        e.next(Biome.REGISTRY.getKeys()).descriptionU("biome");
    }

    public static void biomeType(Expect e)
    {
        e.next(AccessorBiomeDictionary.getMap().keySet()).descriptionU("biome type");
    }

    public static void dimension(Expect e)
    {
        e.next(Arrays.stream(DimensionManager.getIDs())).descriptionU("dimension");
    }

    public static void block(Expect e)
    {
        e.next(Block.REGISTRY.getKeys()).descriptionU("block");
    }

    public static void command(Expect e)
    {
        e.next((server, sender, args, pos) -> server.getCommandManager().getCommands().keySet()).descriptionU("command");
    }

    public static Consumer<Expect> commandArguments(Function<Parameters, Parameter<String>> start)
    {
        return e -> e.nextRaw((server1, sender, params, pos1) ->
        {
            Parameter<String> commandParameter = start.apply(params);
            Optional<ICommand> other = commandParameter.tryGet().map(server1.getCommandManager().getCommands()::get);
            return other.map(c -> c.getTabCompletions(server1, sender, commandParameter.move(1).to(NaP::varargs).get(), pos1)).orElse(Collections.emptyList());
        }).descriptionU("args...");
    }

    public static void entity(Expect e)
    {
        e.next((server, sender, parameters, pos) -> Arrays.stream(server.getOnlinePlayerNames())).descriptionU("entity");
    }

    public static void rotation(Expect e)
    {
        e.any("0", "90", "180", "270").descriptionU("rotation");
    }
}
