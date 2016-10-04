/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandWrite extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "write";
    }

    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    @Override
    public String getCommandUsage(ICommandSender commandSender)
    {
        return ServerTranslations.usage("commands.rcsave.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length < 3)
            throw ServerTranslations.wrongUsageException("commands.rcsave.usage");

        String adapterID = args[0];
        String id = args[1];

        if (!RecurrentComplex.saver.has(adapterID))
            throw ServerTranslations.commandException("commands.rcsave.noregistry");
        if (!RecurrentComplex.saver.registry(adapterID).ids().contains(id))
            throw ServerTranslations.commandException("commands.rcsave.noelement");

        ResourceDirectory directory = ResourceDirectory.valueOf(args[2]);

        if (RCCommands.informSaveResult(RecurrentComplex.saver.trySave(directory.toPath(), adapterID, id), commandSender, directory.subDirectoryName(), adapterID, id))
        {
            RCCommands.informDeleteResult(RecurrentComplex.saver.tryDeleteWithID(directory.opposite().toPath(), adapterID, id), commandSender, adapterID, id, directory.subDirectoryName());

            // Could also predict changes and just reload those for the file but eh.
            ResourceDirectory.reloadCustomFiles(RecurrentComplex.loader);
            ResourceDirectory.reloadServerFiles(RecurrentComplex.loader);
        }
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, RecurrentComplex.saver.keySet());
        else if (args.length == 2)
            return getListOfStringsMatchingLastWord(args, Optional.ofNullable(RecurrentComplex.saver.get(args[0])).map(a -> a.getRegistry().ids()).orElse(Collections.emptySet()));
        else if (args.length == 3)
            return getListOfStringsMatchingLastWord(args, Arrays.asList(ResourceDirectory.values()));

        return super.getTabCompletionOptions(server, sender, args, pos);
    }
}
