/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.capability.StructureEntityInfo;
import ivorius.reccomplex.operation.Operation;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
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

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender commandSender)
    {
        return ServerTranslations.usage("commands.rcpreview.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length < 1)
            throw ServerTranslations.wrongUsageException("commands.rcpreview.usage");

        EntityPlayer player = getCommandSenderAsPlayer(commandSender);
        StructureEntityInfo structureEntityInfo = RCCommands.getStructureEntityInfo(player, null);

        Operation.PreviewType previewType = Operation.PreviewType.find(args[0]);
        if (previewType == null)
            throw ServerTranslations.commandException("commands.rcpreview.invalid");

        structureEntityInfo.setPreviewType(previewType);
        structureEntityInfo.sendPreviewTypeToClients(player);

        commandSender.addChatMessage(ServerTranslations.format("commands.rcpreview.success", args[0]));
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, Operation.PreviewType.keys());

        return Collections.emptyList();
    }
}
