/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.files;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.commands.parameters.RCExpect;
import ivorius.reccomplex.files.loading.LeveledRegistry;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandReload extends CommandBase
{
    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "reload";
    }

    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    @Override
    public String getUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.strucReload.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        try
        {
            LeveledRegistry.Level level = args.length >= 1 ? LeveledRegistry.Level.valueOf(args[0]) : LeveledRegistry.Level.CUSTOM;
            RCCommands.tryReload(RecurrentComplex.loader, level);

            commandSender.sendMessage(ServerTranslations.format("commands.strucReload.success", level));
        }
        catch (IllegalArgumentException e)
        {
            throw ServerTranslations.wrongUsageException("commands.strucReload.usage");
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        return RCExpect.startRC()
                .any(LeveledRegistry.Level.CUSTOM, LeveledRegistry.Level.MODDED, LeveledRegistry.Level.SERVER)
                .get(server, sender, args, pos);
    }
}
