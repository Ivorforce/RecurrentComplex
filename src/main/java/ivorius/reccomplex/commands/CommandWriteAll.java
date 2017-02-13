/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
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
import java.util.*;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandWriteAll extends CommandBase
{
    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "writeall";
    }

    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    @Override
    public String getUsage(ICommandSender commandSender)
    {
        return ServerTranslations.usage("commands.rcsaveall.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length < 2)
            throw ServerTranslations.wrongUsageException("commands.rcsaveall.usage");

        String adapterID = args[0];

        if (!RecurrentComplex.saver.has(adapterID))
            throw ServerTranslations.commandException("commands.rcsaveall.noregistry");

        ResourceDirectory directory = RCCommands.parseResourceDirectory(args[1]);
        Optional<FileSaverAdapter<?>> adapterOptional = Optional.ofNullable(RecurrentComplex.saver.get(adapterID));
        Set<String> ids = adapterOptional.map(a -> a.getRegistry().ids()).orElse(Collections.emptySet());

        ResourceMatcher resourceMatcher = ExpressionCache.of(new ResourceMatcher(id -> adapterOptional.map(a -> a.getRegistry().has(id)).orElse(false)),
                args.length > 2 ? buildString(args, 2) : "");

        int saved = 0, failed = 0;
        for (String id : ids)
        {
            if (!resourceMatcher.test(new RawResourceLocation(id, adapterOptional.map(a -> a.getRegistry().status(id).getDomain()).orElseThrow(IllegalStateException::new))))
                continue;

            boolean success = RecurrentComplex.saver.trySave(directory.toPath(), adapterID, id);

            if (success)
                saved ++;
            else
                failed ++;
        }

        commandSender.sendMessage(ServerTranslations.format("commands.rcsaveall.result", saved, directory, failed));

        ResourceDirectory.reload(RecurrentComplex.loader, LeveledRegistry.Level.CUSTOM);
        ResourceDirectory.reload(RecurrentComplex.loader, LeveledRegistry.Level.SERVER);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, RecurrentComplex.saver.keySet());
        else if (args.length == 2)
            return getListOfStringsMatchingLastWord(args, Arrays.asList(ResourceDirectory.values()));

        return super.getTabCompletions(server, sender, args, pos);
    }
}
