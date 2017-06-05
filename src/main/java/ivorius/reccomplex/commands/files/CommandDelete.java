/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.files;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.commands.parameters.CommandExpecting;
import ivorius.reccomplex.commands.parameters.Expect;
import ivorius.reccomplex.commands.parameters.RCExpect;
import ivorius.reccomplex.commands.parameters.RCParameters;
import ivorius.reccomplex.files.loading.LeveledRegistry;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandDelete extends CommandExpecting
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
    public Expect<?> expect()
    {
        RCExpect<?> expect = RCExpect.expectRC();

        expect.next(RecurrentComplex.saver.keySet());
        expect.next(params -> params.get().first().tryGet().map(RecurrentComplex.saver::get).map(a -> a.getRegistry().ids()));
        expect.named("directory", "d").resourceDirectory();

        return expect;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args, expect()::declare);

        String adapterID = parameters.get().first().require();
        String id = parameters.get().at(1).require();

        if (!RecurrentComplex.saver.has(adapterID))
            throw ServerTranslations.commandException("commands.rcsave.noregistry");
        if (!RecurrentComplex.saver.registry(adapterID).ids().contains(id))
            throw ServerTranslations.commandException("commands.rcsave.noelement");

        ResourceDirectory directory = parameters.rc("directory").resourceDirectory().require();

        RCCommands.informDeleteResult(RecurrentComplex.saver.tryDeleteWithID(directory.toPath(), adapterID, id), commandSender, adapterID, id, directory);

        // Could also predict changes and just reload those for the file but eh.
        RCCommands.tryReload(RecurrentComplex.loader, LeveledRegistry.Level.CUSTOM);
        RCCommands.tryReload(RecurrentComplex.loader, LeveledRegistry.Level.SERVER);
    }
}
