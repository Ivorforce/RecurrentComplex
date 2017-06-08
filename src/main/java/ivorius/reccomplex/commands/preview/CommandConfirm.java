/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.preview;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.capability.RCEntityInfo;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.mcopts.commands.SimpleCommand;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandConfirm extends SimpleCommand
{
    public CommandConfirm()
    {
        super(RCConfig.commandPrefix + "confirm");
        permitFor(2);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        EntityPlayer player = getCommandSenderAsPlayer(commandSender);
        RCEntityInfo RCEntityInfo = RCCommands.getStructureEntityInfo(player, null);

        if (!RCEntityInfo.performOperation((WorldServer) commandSender.getEntityWorld(), player))
            throw RecurrentComplex.translations.commandException("commands.rc.noOperation");
    }
}
