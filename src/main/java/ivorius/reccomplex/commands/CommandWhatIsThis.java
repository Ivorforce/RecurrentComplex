/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import com.google.common.collect.Lists;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.StructureGenerationData;
import joptsimple.internal.Strings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandWhatIsThis extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "whatisthis";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.whatisthis.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        World world = commandSender.getEntityWorld();

        BlockPos pos = RCCommands.tryParseBlockPos(commandSender, args, 0, false);

        Collection<StructureGenerationData.Entry> entries = StructureGenerationData.get(world).getEntriesAt(pos);
        if (entries.size() > 0)
        {
            List<StructureGenerationData.Entry> ordered = Lists.newArrayList(entries);
            if (ordered.size() > 1)
                commandSender.addChatMessage(ServerTranslations.format("commands.whatisthis.many", Strings.join(Lists.transform(ordered, StructureGenerationData.Entry::getStructureID), ", ")));
            else
                commandSender.addChatMessage(ServerTranslations.format("commands.whatisthis.one", ordered.get(0).getStructureID()));
        }
        else
            commandSender.addChatMessage(ServerTranslations.format("commands.whatisthis.none"));
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1 || args.length == 2 || args.length == 3)
            return getTabCompletionCoordinate(args, args.length, pos);

        return Collections.emptyList();
    }
}
