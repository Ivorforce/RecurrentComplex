/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.commands.parameters.*;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.utils.expression.DependencyExpression;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.util.stream.Collectors;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandEval extends CommandExpecting
{
    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "eval";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public Expect<?> expect()
    {
        return RCExpect.expectRC()
                .next(RCConfig.globalToggles.keySet().stream().map(s -> "global:" + s).collect(Collectors.toSet())).descriptionU("dependency expression");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args, expect()::declare);

        DependencyExpression matcher = parameters.get().expression(new DependencyExpression()).require();

        boolean result = matcher.test(RecurrentComplex.saver);
        commandSender.sendMessage(ServerTranslations.get(result ? "commands.rceval.result.true" : "commands.rceval.result.false"));
    }
}
