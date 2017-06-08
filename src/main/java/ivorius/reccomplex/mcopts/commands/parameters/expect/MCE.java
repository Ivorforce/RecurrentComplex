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
                .description("commands.parameters.x");
    }

    public static void y(Expect e)
    {
        e.nextRaw((ser, sen, args, pos) -> CommandBase.getTabCompletionCoordinate(args.lastAsArray(), -1, pos))
                .description("commands.parameters.y");
    }

    public static void z(Expect e)
    {
        e.nextRaw((ser, sen, args, pos) -> CommandBase.getTabCompletionCoordinate(args.lastAsArray(), -2, pos))
                .description("commands.parameters.z");
    }

    public static void biome(Expect e)
    {
        e.next(Biome.REGISTRY.getKeys()).description("commands.parameters.biome");
    }

    public static void biomeType(Expect e)
    {
        e.next(AccessorBiomeDictionary.getMap().keySet()).description("commands.parameters.biometype");
    }

    public static void dimension(Expect e)
    {
        e.next(Arrays.stream(DimensionManager.getIDs())).description("commands.parameters.dimension");
    }

    public static void block(Expect e)
    {
        e.next(Block.REGISTRY.getKeys()).description("commands.parameters.block");
    }

    public static void command(Expect e)
    {
        e.next((server, sender, args, pos) -> server.getCommandManager().getCommands().keySet()).description("commands.parameters.command");
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
        e.next((server, sender, parameters, pos) -> Arrays.stream(server.getOnlinePlayerNames())).description("commands.parameters.entity");
    }

    public static void rotation(Expect e)
    {
        e.any("0", "90", "180", "270").description("commands.parameters.rotation");
    }
}
