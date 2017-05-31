/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.commands.parameters.RCExpect;
import ivorius.reccomplex.commands.parameters.RCParameters;
import ivorius.reccomplex.files.loading.LeveledRegistry;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.files.saving.FileSaverAdapter;
import ivorius.reccomplex.utils.RawResourceLocation;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.utils.algebra.ExpressionCache;
import ivorius.reccomplex.utils.expression.ResourceMatcher;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandWriteAll extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "writeall";
    }

    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    @Override
    public String getCommandUsage(ICommandSender commandSender)
    {
        return ServerTranslations.usage("commands.rcsaveall.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args);

        String adapterID = parameters.get().first().require();

        if (!RecurrentComplex.saver.has(adapterID))
            throw ServerTranslations.commandException("commands.rcsaveall.noregistry");

        ResourceDirectory directory = parameters.rc("dir").resourceDirectory().optional().orElse(ResourceDirectory.ACTIVE);
        Optional<FileSaverAdapter<?>> adapterOptional = Optional.ofNullable(RecurrentComplex.saver.get(adapterID));
        Set<String> ids = adapterOptional.map(a -> a.getRegistry().ids()).orElse(Collections.emptySet());

        ResourceMatcher resourceMatcher = ExpressionCache.of(new ResourceMatcher(id -> adapterOptional.map(a -> a.getRegistry().has(id)).orElse(false)),
                parameters.get().at(1).require());

        int saved = 0, failed = 0;
        for (String id : ids)
        {
            if (!resourceMatcher.test(new RawResourceLocation(adapterOptional.map(a -> a.getRegistry().status(id).getDomain()).orElseThrow(IllegalStateException::new), id)))
                continue;

            boolean success = RecurrentComplex.saver.trySave(directory.toPath(), adapterID, id);

            if (success)
                saved++;
            else
                failed++;
        }

        commandSender.addChatMessage(ServerTranslations.format("commands.rcsaveall.result", saved, directory, failed));

        RCCommands.tryReload(RecurrentComplex.loader, LeveledRegistry.Level.CUSTOM);
        RCCommands.tryReload(RecurrentComplex.loader, LeveledRegistry.Level.SERVER);
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        RCParameters parameters = RCParameters.of(args);

        RCExpect<?> expect = RCExpect.startRC();
        // Can't chain because of compiler bug :|

        expect.next(RecurrentComplex.saver.keySet());
        expect.next(args1 -> parameters.get().first().optional().map(RecurrentComplex.saver::get).map(a -> a.getRegistry().ids()).orElse(Collections.emptySet()));
        expect.named("dir").resourceDirectory();

        return expect.get(server, sender, args, pos);
    }
}
