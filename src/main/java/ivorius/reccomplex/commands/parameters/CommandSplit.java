/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import com.google.common.collect.Lists;
import joptsimple.internal.Strings;
import net.minecraft.command.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Created by lukas on 01.06.17.
 */
public abstract class CommandSplit extends CommandBase
{
    protected final Map<String, ICommand> commands = new HashMap<>();

    public CommandSplit()
    {
        add(new SimpleCommand("help", "<command>", () -> RCExpect.startRC().next(commands.keySet()))
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
            {
                IvParameters parameters = IvParameters.of(args);
                throw new WrongUsageException(parameters.get().first().map(commands::get).optional()
                        .orElse(CommandSplit.this).getUsage(sender)
                );
            }
        });
    }

    public void add(ICommand command)
    {
        commands.put(command.getName(), command);
    }

    public Optional<ICommand> get(String name)
    {
        return Optional.ofNullable(commands.get(name));
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return String.format("/%s <%s>", getName(),
                Strings.join(Lists.newArrayList(commands.keySet()), "|"));
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 1)
            throw new WrongUsageException(getUsage(sender));

        ICommand iCommand = get(args[0]).orElseThrow(() -> new CommandException("Unknown command: " + args[0]));
        iCommand.execute(server, sender, Arrays.copyOfRange(args, 1, args.length));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, commands.keySet());
        return get(args[0]).map(c -> c.getTabCompletions(server, sender, Arrays.copyOfRange(args, 1, args.length), targetPos))
                .orElse(Collections.emptyList());
    }
}
