/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.files;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.mcopts.commands.CommandExpecting;
import ivorius.reccomplex.mcopts.commands.parameters.*;
import ivorius.reccomplex.mcopts.commands.parameters.expect.Expect;
import ivorius.reccomplex.files.loading.LeveledRegistry;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandReload extends CommandExpecting
{
    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "reload";
    }

    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    @Override
    public Expect expect()
    {
        return Parameters.expect()
                .any(LeveledRegistry.Level.CUSTOM, LeveledRegistry.Level.MODDED, LeveledRegistry.Level.SERVER).descriptionU("level");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        Parameters parameters = Parameters.of(args, expect()::declare);

        LeveledRegistry.Level level = parameters.get(0).map(LeveledRegistry.Level::valueOf).optional().orElse(LeveledRegistry.Level.CUSTOM);

        try
        {
            RCCommands.tryReload(RecurrentComplex.loader, level);

            commandSender.sendMessage(RecurrentComplex.translations.format("commands.strucReload.success", level));
        }
        catch (IllegalArgumentException e)
        {
            throw RecurrentComplex.translations.wrongUsageException("commands.strucReload.usage");
        }
    }
}
