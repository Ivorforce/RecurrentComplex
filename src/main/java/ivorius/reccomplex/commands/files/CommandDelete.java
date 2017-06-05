/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.files;

import com.google.common.collect.Lists;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.commands.parameters.CommandExpecting;
import ivorius.reccomplex.commands.parameters.Expect;
import ivorius.reccomplex.commands.parameters.RCExpect;
import ivorius.reccomplex.commands.parameters.RCParameters;
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

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandDelete extends CommandExpecting
{
    @Override
    public String getName()
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

        expect.next(RecurrentComplex.saver.keySet()).requiredU("file type");
        expect.next(params -> params.get().first().tryGet().map(RecurrentComplex.saver::get).map(a -> a.getRegistry().ids())).optionalU("resource expression").repeat();
        expect.named("directory", "d").resourceDirectory().required();

        return expect;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args, expect()::declare);

        String adapterID = parameters.get().first().require();

        if (!RecurrentComplex.saver.has(adapterID))
            throw ServerTranslations.commandException("commands.rcsave.noregistry");

        ResourceDirectory directory = parameters.rc("directory").resourceDirectory().require();
        Optional<FileSaverAdapter<?>> adapterOptional = Optional.ofNullable(RecurrentComplex.saver.get(adapterID));
        Collection<String> ids = Lists.newArrayList(adapterOptional.map(a -> a.getRegistry().ids()).orElse(Collections.emptySet()));

        ResourceExpression resourceExpression = ExpressionCache.of(new ResourceExpression(id -> adapterOptional.map(a -> a.getRegistry().has(id)).orElse(false)),
                parameters.get().move(1).text().require());

        for (String id : ids)
        {
            if (!resourceExpression.test(new RawResourceLocation(adapterOptional.map(a -> a.getRegistry().status(id).getDomain()).orElseThrow(IllegalStateException::new), id)))
                continue;

            RCCommands.informDeleteResult(RecurrentComplex.saver.tryDeleteWithID(directory.toPath(), adapterID, id), commandSender, adapterID, id, directory);

            // Could also predict changes and just reload those for the file but eh.
            RCCommands.tryReload(RecurrentComplex.loader, LeveledRegistry.Level.CUSTOM);
            RCCommands.tryReload(RecurrentComplex.loader, LeveledRegistry.Level.SERVER);
        }
    }
}
