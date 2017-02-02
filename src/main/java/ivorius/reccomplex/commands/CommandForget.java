/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.WorldStructureGenerationData;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandForget extends CommandBase
{
    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "forget";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.rcforget.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length < 1)
            throw ServerTranslations.wrongUsageException("commands.rcforget.usage");

        WorldStructureGenerationData generationData = WorldStructureGenerationData.get(commandSender.getEntityWorld());
        WorldStructureGenerationData.Entry entry = generationData.removeEntry(UUID.fromString(args[0]));

        if (entry == null)
            throw ServerTranslations.commandException("commands.rcforget.unknown");
        else
            commandSender.sendMessage(ServerTranslations.format("commands.rcforget.success", entry.description()));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        return Collections.emptyList();
    }
}
