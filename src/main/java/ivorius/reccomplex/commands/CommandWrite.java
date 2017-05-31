/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.commands.parameters.Expect;
import ivorius.reccomplex.commands.parameters.Parameters;
import ivorius.reccomplex.files.loading.LeveledRegistry;
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
    public String getName()
    {
        return RCConfig.commandPrefix + "write";
    }

    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    @Override
    public String getUsage(ICommandSender commandSender)
    {
        return ServerTranslations.usage("commands.rcsave.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        Parameters parameters = Parameters.of(this, args);

        String adapterID = parameters.get().at(0).require();
        String id = parameters.get().at(1).require();

        if (!RecurrentComplex.saver.has(adapterID))
            throw ServerTranslations.commandException("commands.rcsave.noregistry");
        if (!RecurrentComplex.saver.registry(adapterID).ids().contains(id))
            throw ServerTranslations.commandException("commands.rcsave.noelement");

        ResourceDirectory directory = parameters.get("dir").resourceDirectory().optional().orElse(ResourceDirectory.ACTIVE);

        if (RCCommands.informSaveResult(RecurrentComplex.saver.trySave(directory.toPath(), adapterID, id), commandSender, directory, adapterID, id))
        {
            RCCommands.informDeleteResult(RecurrentComplex.saver.tryDeleteWithID(directory.opposite().toPath(), adapterID, id), commandSender, adapterID, id, directory);

            // Could also predict changes and just reload those for the file but eh.
            RCCommands.tryReload(RecurrentComplex.loader, LeveledRegistry.Level.CUSTOM);
            RCCommands.tryReload(RecurrentComplex.loader, LeveledRegistry.Level.SERVER);
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        Parameters parameters = Parameters.of(this, args);

        return Expect.start()
                .next(RecurrentComplex.saver.keySet())
                .next(args1 -> parameters.get().at(0).optional().map(RecurrentComplex.saver::get).map(a -> a.getRegistry().ids()).orElse(Collections.emptySet()))
                .named("dir").resourceDirectory()
                .get(server, sender, args, pos);
    }
}
