/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.world.MockWorld;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 * Created by lukas on 13.02.17.
 */
public abstract class CommandVirtual extends CommandBase
{
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        execute(new MockWorld.Real(sender.getEntityWorld()), sender, args);
    }

    public abstract void execute(MockWorld world, ICommandSender commandSender, String[] args) throws CommandException;
}
