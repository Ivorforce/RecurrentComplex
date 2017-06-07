/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.world.MockWorld;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 * Created by lukas on 13.02.17.
 */
public interface CommandVirtual extends ICommand
{
    @Override
    default void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        execute(new MockWorld.Real(sender.getEntityWorld()), sender, args);
    }

    void execute(MockWorld world, ICommandSender sender, String[] args) throws CommandException;
}
