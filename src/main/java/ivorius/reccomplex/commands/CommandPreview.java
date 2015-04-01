/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.operation.Operation;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;

import java.util.List;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandPreview extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "preview";
    }

    @Override
    public String getCommandUsage(ICommandSender commandSender)
    {
        return "commands.rcpreview.usage";
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args)
    {
        if (args.length < 1)
            throw new WrongUsageException("commands.rcpreview.usage");

        EntityPlayer player = getCommandSenderAsPlayer(commandSender);
        StructureEntityInfo structureEntityInfo = RCCommands.getStructureEntityInfo(player);

        Operation.PreviewType previewType = Operation.PreviewType.find(args[0]);
        if (previewType == null)
            throw new CommandException("commands.rcpreview.invalid");

        structureEntityInfo.setPreviewType(previewType);
        structureEntityInfo.sendPreviewTypeToClients(player);

        commandSender.addChatMessage(new ChatComponentTranslation("commands.rcpreview.success", args[0]));
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, Operation.PreviewType.keys());

        return null;
    }
}
