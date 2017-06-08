/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.files;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.commands.RCTextStyle;
import ivorius.reccomplex.commands.parameters.CommandExpecting;
import ivorius.reccomplex.commands.parameters.NaP;
import ivorius.reccomplex.commands.parameters.Parameters;
import ivorius.reccomplex.commands.parameters.expect.Expect;
import ivorius.reccomplex.commands.rcparameters.RCP;
import ivorius.reccomplex.commands.rcparameters.expect.RCE;
import ivorius.reccomplex.files.loading.LeveledRegistry;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.files.saving.FileSaverAdapter;
import ivorius.reccomplex.utils.RawResourceLocation;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.utils.algebra.ExpressionCache;
import ivorius.reccomplex.utils.expression.ResourceExpression;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandWrite extends CommandExpecting
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
    public Expect expect()
    {
        return Parameters.expect()
                .next(RecurrentComplex.saver.keySet()).descriptionU("file type").required()
                .next(params -> params.get(0).tryGet().map(RecurrentComplex.saver::get).map(a -> a.getRegistry().ids())).descriptionU("resource expression").repeat()
                .named("directory", "d").then(RCE::resourceDirectory);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        Parameters parameters = Parameters.of(args, expect()::declare);

        String adapterID = parameters.get(0).require();

        if (!RecurrentComplex.saver.has(adapterID))
            throw ServerTranslations.commandException("commands.rcsaveall.noregistry");

        ResourceDirectory directory = parameters.get("directory").to(RCP::resourceDirectory).optional().orElse(ResourceDirectory.ACTIVE);
        Optional<FileSaverAdapter<?>> adapterOptional = Optional.ofNullable(RecurrentComplex.saver.get(adapterID));
        Set<String> ids = adapterOptional.map(a -> a.getRegistry().ids()).orElse(Collections.emptySet());

        ResourceExpression resourceExpression = ExpressionCache.of(new ResourceExpression(id -> adapterOptional.map(a -> a.getRegistry().has(id)).orElse(false)),
                parameters.get(1).rest(NaP.join()).require());

        int saved = 0, failed = 0;
        for (String id : ids)
        {
            if (!resourceExpression.test(new RawResourceLocation(adapterOptional.map(a -> a.getRegistry().status(id).getDomain()).orElseThrow(IllegalStateException::new), id)))
                continue;

            boolean success = RecurrentComplex.saver.trySave(directory.toPath(), adapterID, id);

            if (success)
                saved++;
            else
                failed++;
        }

        commandSender.sendMessage(ServerTranslations.format("commands.rcsaveall.result", saved, RCTextStyle.path(directory), failed));

        RCCommands.tryReload(RecurrentComplex.loader, LeveledRegistry.Level.CUSTOM);
        RCCommands.tryReload(RecurrentComplex.loader, LeveledRegistry.Level.SERVER);
    }
}
