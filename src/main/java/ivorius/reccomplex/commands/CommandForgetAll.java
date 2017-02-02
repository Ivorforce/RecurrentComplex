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
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandForgetAll extends CommandBase
{
    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "forgetall";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.rcforgetall.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        World world = commandSender.getEntityWorld();

        BlockPos pos = RCCommands.tryParseBlockPos(commandSender, args, 0, false);

        WorldStructureGenerationData generationData = WorldStructureGenerationData.get(world);
        List<WorldStructureGenerationData.Entry> entries = generationData.entriesAt(pos).collect(Collectors.toList());

        entries.forEach(e -> generationData.removeEntry(e.getUuid()));

        if (entries.size() == 1)
            commandSender.sendMessage(ServerTranslations.format("commands.rcforget.success", entries.get(0).description()));
        else
            commandSender.sendMessage(ServerTranslations.format("commands.rcforgetall.success", entries.size()));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1 || args.length == 2 || args.length == 3)
            return getTabCompletionCoordinate(args, 0, pos);

        return Collections.emptyList();
    }
}
