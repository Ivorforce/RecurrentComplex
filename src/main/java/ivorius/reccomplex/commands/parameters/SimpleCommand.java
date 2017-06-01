/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by lukas on 01.06.17.
 */
public abstract class SimpleCommand extends CommandBase
{
    public String name;

    public String usage;
    public Supplier<Expect<?>> expector;

    public SimpleCommand(String name)
    {
        this.name = name;
    }

    public SimpleCommand(String name, String usage, Supplier<Expect<?>> expector)
    {
        this.name = name;
        this.usage = usage;
        this.expector = expector;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return String.format("%s %s", name, usage);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        return expector.get().get(server, sender, args, targetPos);
    }
}
