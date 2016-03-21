/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.worldgen.StructureGenerationData;
import joptsimple.internal.Strings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collection;
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

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.whatisthis.usage");
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args)
    {
        int x, y, z;

        World world = commandSender.getEntityWorld();

        x = commandSender.getPlayerCoordinates().posX;
        y = commandSender.getPlayerCoordinates().posY;
        z = commandSender.getPlayerCoordinates().posZ;

        if (args.length >= 3)
        {
            x = MathHelper.floor_double(func_110666_a(commandSender, (double) x, args[0]));
            y = MathHelper.floor_double(func_110666_a(commandSender, (double) y, args[1]));
            z = MathHelper.floor_double(func_110666_a(commandSender, (double) z, args[2]));
        }

        Collection<StructureGenerationData.Entry> entries = StructureGenerationData.get(world).getEntriesAt(new BlockCoord(x, y, z));
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
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args)
    {
        if (args.length == 1 || args.length == 2 || args.length == 3)
        {
            return getListOfStringsMatchingLastWord(args, "~");
        }

        return null;
    }
}
