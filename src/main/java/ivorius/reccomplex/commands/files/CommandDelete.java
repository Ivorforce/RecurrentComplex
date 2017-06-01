/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.files;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.commands.parameters.RCExpect;
import ivorius.reccomplex.commands.parameters.RCParameters;
import ivorius.reccomplex.files.loading.LeveledRegistry;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandDelete extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "delete";
    }

    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    @Override
    public String getCommandUsage(ICommandSender commandSender)
    {
        return ServerTranslations.usage("commands.rcdelete.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args);

        String adapterID = parameters.get().first().require();
        String id = parameters.get().at(1).require();

        if (!RecurrentComplex.saver.has(adapterID))
            throw ServerTranslations.commandException("commands.rcsave.noregistry");
        if (!RecurrentComplex.saver.registry(adapterID).ids().contains(id))
            throw ServerTranslations.commandException("commands.rcsave.noelement");

        ResourceDirectory directory = parameters.rc("dir").resourceDirectory().require();

        RCCommands.informDeleteResult(RecurrentComplex.saver.tryDeleteWithID(directory.toPath(), adapterID, id), commandSender, adapterID, id, directory);

        // Could also predict changes and just reload those for the file but eh.
        RCCommands.tryReload(RecurrentComplex.loader, LeveledRegistry.Level.CUSTOM);
        RCCommands.tryReload(RecurrentComplex.loader, LeveledRegistry.Level.SERVER);
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        RCExpect<?> expect = RCExpect.startRC();

        expect.next(RecurrentComplex.saver.keySet());
        expect.next(Optional.ofNullable(RecurrentComplex.saver.get(args[0])).map(a -> a.getRegistry().ids()).orElse(Collections.emptySet()));
        expect.named("dir").resourceDirectory();

        return expect.get(server, sender, args, pos);
    }
}
