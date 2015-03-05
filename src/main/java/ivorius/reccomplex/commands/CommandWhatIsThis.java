/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.operation.OperationRegistry;
import ivorius.reccomplex.structures.OperationGenerateStructure;
import ivorius.reccomplex.structures.StructureInfo;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import ivorius.reccomplex.worldgen.StructureGenerationData;
import joptsimple.internal.Strings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
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
        return "commands.whatisthis.usage";
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

        Collection<StructureGenerationData.Entry> entries = StructureGenerationData.get(world).getEntries(new BlockCoord(x, y, z));
        if (entries.size() > 0)
        {
            List<StructureGenerationData.Entry> ordered = Lists.newArrayList(entries);
            if (ordered.size() > 1)
                commandSender.addChatMessage(new ChatComponentTranslation("commands.whatisthis.many", Strings.join(Lists.transform(ordered, new Function<StructureGenerationData.Entry, String>()
                {
                    @Nullable
                    @Override
                    public String apply(StructureGenerationData.Entry input)
                    {
                        return input.getStructureID();
                    }
                }), ", ")));
            else
                commandSender.addChatMessage(new ChatComponentTranslation("commands.whatisthis.one", ordered.get(0).getStructureID()));
        }
        else
            commandSender.addChatMessage(new ChatComponentTranslation("commands.whatisthis.none"));
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
