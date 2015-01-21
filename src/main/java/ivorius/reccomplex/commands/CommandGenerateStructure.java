/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.worldgen.StructureRegistry;
import ivorius.reccomplex.worldgen.StructureInfo;
import ivorius.reccomplex.worldgen.WorldGenStructures;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.List;
import java.util.Set;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandGenerateStructure extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "strucGen";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return "commands.strucGen.usage";
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args)
    {
        int x, z;

        if (args.length <= 0)
        {
            throw new WrongUsageException("commands.strucGen.usage");
        }

        String structureName = args[0];
        StructureInfo structureInfo = StructureRegistry.getStructure(structureName);
        World world = commandSender.getEntityWorld();

        if (structureInfo == null)
        {
            throw new WrongUsageException("commands.strucGen.noStructure", structureName);
        }

        x = commandSender.getPlayerCoordinates().posX;
        z = commandSender.getPlayerCoordinates().posZ;

        if (args.length >= 3)
        {
            x = MathHelper.floor_double(func_110666_a(commandSender, (double) x, args[1]));
            z = MathHelper.floor_double(func_110666_a(commandSender, (double) z, args[2]));
        }

        WorldGenStructures.generateStructureRandomly(world, world.rand, structureInfo, x, z, false);
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args)
    {
        if (args.length == 1)
        {
            Set<String> allStructureNames = StructureRegistry.getAllStructureNames();

            return getListOfStringsMatchingLastWord(args, allStructureNames.toArray(new String[allStructureNames.size()]));
        }
        else if (args.length == 2 || args.length == 3)
        {
            return getListOfStringsMatchingLastWord(args, "~");
        }

        return null;
    }
}
