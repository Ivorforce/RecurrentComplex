/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.files.LeveledRegistry;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandReload extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "reload";
    }

    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.strucReload.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        LeveledRegistry.Level level = args.length >= 1 ? LeveledRegistry.Level.valueOf(args[0]) : LeveledRegistry.Level.CUSTOM;

        switch (level)
        {
            case CUSTOM:
                RecurrentComplex.fileTypeRegistry.loadCustomFiles();
                break;
            case MODDED:
                RecurrentComplex.fileTypeRegistry.loadModFiles();
                break;
            default:
                throw ServerTranslations.wrongUsageException("commands.strucReload.usage");
        }

        commandSender.addChatMessage(ServerTranslations.format("commands.strucReload.success", level));
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, Arrays.asList(LeveledRegistry.Level.CUSTOM, LeveledRegistry.Level.MODDED));

        return Collections.emptyList();
    }
}
