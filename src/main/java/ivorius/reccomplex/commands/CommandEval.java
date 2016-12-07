/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.utils.expression.DependencyMatcher;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandEval extends CommandBase
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
    public String getUsage(ICommandSender commandSender)
    {
        return ServerTranslations.usage("commands.rceval.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length < 1)
            throw ServerTranslations.wrongUsageException("commands.rceval.usage");

        DependencyMatcher matcher = new DependencyMatcher(buildString(args, 0));
        if (!matcher.isExpressionValid())
        {
            commandSender.sendMessage(ServerTranslations.format("commands.rceval.error", matcher.getParseException().getMessage()));
            return;
        }

        boolean result = matcher.test(RecurrentComplex.saver);
        commandSender.sendMessage(ServerTranslations.get(result ? "commands.rceval.result.true" : "commands.rceval.result.false"));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        return getListOfStringsMatchingLastWord(args, RCConfig.globalToggles.keySet().stream().map(s -> "global:" + s).collect(Collectors.toSet()));
    }
}
