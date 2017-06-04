/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by lukas on 01.06.17.
 */
public abstract class SimpleCommand extends CommandBase implements Expecting
{
    public String name;

    public String usage;
    public Supplier<Expect<?>> expector;

    public int permissionLevel = 4;

    public SimpleCommand(String name)
    {
        this.name = name;
    }

    public SimpleCommand(String name, Supplier<Expect<?>> expector)
    {
        this.name = name;
        this.usage =  expector.get().usage();
        this.expector = expector;
    }

    public SimpleCommand(String name, String usage, Supplier<Expect<?>> expector)
    {
        this.name = name;
        this.usage = usage;
        this.expector = expector;
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return permissionLevel;
    }

    public SimpleCommand permitFor(int level)
    {
        this.permissionLevel = level;
        return this;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public Expect<?> expect()
    {
        return expector.get();
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return String.format("%s %s", name, usage);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        return expect().get(server, sender, args, targetPos);
    }
}
