/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.capability.StructureEntityInfo;
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
public class CommandVisual extends CommandBase
{
    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "visual";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender commandSender)
    {
        return ServerTranslations.usage("commands.rcvisual.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length < 2)
            throw ServerTranslations.wrongUsageException("commands.rcvisual.usage");

        boolean enabled = parseBoolean(args[1]);

        EntityPlayer player = getCommandSenderAsPlayer(commandSender);
        StructureEntityInfo structureEntityInfo = RCCommands.getStructureEntityInfo(player, null);

        switch (args[0])
        {
            case "rulers":
                structureEntityInfo.showGrid = enabled;
                structureEntityInfo.sendOptionsToClients(player);
                break;
            default:
                throw ServerTranslations.wrongUsageException("commands.rcvisual.usage");
        }

        if (enabled)
            commandSender.sendMessage(ServerTranslations.format("commands.rcvisual.enabled", args[0]));
        else
            commandSender.sendMessage(ServerTranslations.format("commands.rcvisual.disabled", args[0]));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, "rulers");
        if (args.length == 2)
            return getListOfStringsMatchingLastWord(args, "true", "false");

        return Collections.emptyList();
    }
}
