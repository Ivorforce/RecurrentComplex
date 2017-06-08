/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.mcopts.commands.CommandExpecting;
import ivorius.mcopts.commands.parameters.*;
import ivorius.mcopts.commands.parameters.expect.Expect;
import ivorius.reccomplex.commands.parameters.RCP;
import ivorius.reccomplex.utils.expression.DependencyExpression;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

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
    public Expect expect()
    {
        return Parameters.expect()
                .next(RCConfig.globalToggles.keySet().stream().map(s -> "global:" + s)).descriptionU("dependency expression");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        Parameters parameters = Parameters.of(args, expect()::declare);

        DependencyExpression matcher = parameters.get(0).rest(NaP.join()).to(RCP.expression(new DependencyExpression())).require();

        boolean result = matcher.test(RecurrentComplex.saver);
        commandSender.sendMessage(RecurrentComplex.translations.get(result ? "commands.rceval.result.true" : "commands.rceval.result.false"));
    }
}
