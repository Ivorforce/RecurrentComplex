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
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandReload extends CommandExpecting
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "reload";
    }

    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    @Override
    public Expect<?> expect()
    {
        return RCExpect.expectRC()
                .any(LeveledRegistry.Level.CUSTOM, LeveledRegistry.Level.MODDED, LeveledRegistry.Level.SERVER).optionalU("level");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args, expect()::declare);

        LeveledRegistry.Level level = parameters.get().first().map(LeveledRegistry.Level::valueOf).optional().orElse(LeveledRegistry.Level.CUSTOM);

        try
        {
            RCCommands.tryReload(RecurrentComplex.loader, level);

            commandSender.addChatMessage(ServerTranslations.format("commands.strucReload.success", level));
        }
        catch (IllegalArgumentException e)
        {
            throw ServerTranslations.wrongUsageException("commands.strucReload.usage");
        }
    }
}
