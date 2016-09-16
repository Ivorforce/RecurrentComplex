/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static ivorius.reccomplex.commands.CommandGenerateStructure.generateStructure;
import static ivorius.reccomplex.commands.CommandGenerateStructure.tabCompletionOptions;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandGenerateStructureAt extends CommandBase
{
    @Nonnull
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "genat";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.strucGenAt.usage");
    }

    @Override
    @ParametersAreNonnullByDefault
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length <= 1)
            throw ServerTranslations.wrongUsageException("commands.strucGenAt.usage");

        Entity entity = getEntity(server, commandSender, args[1]);

        generateStructure(commandSender, args, 0, 2, entity, 4, 5);
    }

    @Nonnull
    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 2)
            return getListOfStringsMatchingLastWord(args, server.getAllUsernames());

        return tabCompletionOptions(args, 0, 2, 3);
    }
}
