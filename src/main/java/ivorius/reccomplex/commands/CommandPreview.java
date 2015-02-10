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

        structureEntityInfo.previewType = getPreviewTypeFromCommand(args[0]);
        structureEntityInfo.sendPreviewTypeToClients(player);

        commandSender.addChatMessage(new ChatComponentTranslation("commands.rcpreview.success", args[0]));
    }

    public int getPreviewTypeFromCommand(String type)
    {
        switch (type)
        {
            case "none":
                return Operation.PREVIEW_TYPE_NONE;
            case "bounds":
                return Operation.PREVIEW_TYPE_BOUNDING_BOX;
            default:
                throw new CommandException("commands.rcpreview.invalid");
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, "none", "bounds");

        return null;
    }
}
