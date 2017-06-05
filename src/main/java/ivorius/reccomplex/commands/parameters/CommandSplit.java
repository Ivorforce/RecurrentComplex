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
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lukas on 01.06.17.
 */
public class CommandSplit extends CommandBase
{
    protected final Map<String, ICommand> commands = new HashMap<>();
    protected String name;
    protected int requiredPermission;

    public CommandSplit()
    {
        add(new SimpleCommand("help", () -> RCExpect.expectRC().next(commands.keySet()).required("command"))
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
            {
                Parameters parameters = Parameters.of(args, null);
                throw new WrongUsageException(parameters.get().first().map(commands::get).optional()
                        .orElse(CommandSplit.this).getUsage(sender)
                );
            }
        });
    }

    public CommandSplit(String name, ICommand... commands)
    {
        this();
        this.name = name;
        Arrays.stream(commands).forEach(this::add);
    }

    public CommandSplit(String name)
    {
        this();
        this.name = name;
    }

    public void add(ICommand command)
    {
        commands.put(command.getName(), command);
    }

    public Optional<ICommand> get(String name)
    {
        return Optional.ofNullable(commands.get(name));
    }

    public CommandSplit permitFor(int permission)
    {
        this.requiredPermission = permission;
        return this;
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return String.format("%s %s%s<%s>", getName(), TextFormatting.RESET, TextFormatting.YELLOW,
                Strings.join(Lists.newArrayList(commands.keySet()), "|"));
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 1)
            throw new WrongUsageException(getUsage(sender));

        ICommand iCommand = get(args[0]).orElseThrow(() -> new CommandException("Unknown command: " + args[0]));

        if (!iCommand.checkPermission(server, sender))
            throw new CommandException("commands.generic.permission");

        iCommand.execute(server, sender, Arrays.copyOfRange(args, 1, args.length));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, commands.entrySet().stream()
                    .filter(e -> e.getValue().checkPermission(server, sender))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList())
            );
        return get(args[0]).map(c -> c.getTabCompletions(server, sender, Arrays.copyOfRange(args, 1, args.length), targetPos))
                .orElse(Collections.emptyList());
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return requiredPermission;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        // If any can use it, we show it
        return super.checkPermission(server, sender) && commands.entrySet().stream()
                .filter(e -> !e.getKey().equals("help"))
                .map(Map.Entry::getValue)
                .anyMatch(c -> c.checkPermission(server, sender));
    }
}
