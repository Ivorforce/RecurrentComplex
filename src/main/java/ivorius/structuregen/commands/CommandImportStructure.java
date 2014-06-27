/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.commands;

import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.structuregen.worldgen.StructureHandler;
import ivorius.structuregen.worldgen.StructureInfo;
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
public class CommandImportStructure extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "strucImport";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return "commands.strucImport.usage";
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args)
    {
        int x, y, z;

        if (args.length <= 0)
        {
            throw new WrongUsageException("commands.strucImport.usage");
        }

        String structureName = args[0];
        StructureInfo structureInfo = StructureHandler.getStructure(structureName);
        World world = commandSender.getEntityWorld();

        if (structureInfo == null)
        {
            throw new WrongUsageException("commands.strucImport.noStructure", structureName);
        }

        x = commandSender.getPlayerCoordinates().posX;
        y = commandSender.getPlayerCoordinates().posY;
        z = commandSender.getPlayerCoordinates().posZ;

        if (args.length >= 4)
        {
            x = MathHelper.floor_double(func_110666_a(commandSender, (double) x, args[1]));
            y = MathHelper.floor_double(func_110666_a(commandSender, (double) x, args[2]));
            z = MathHelper.floor_double(func_110666_a(commandSender, (double) z, args[3]));
        }

        structureInfo.generateSource(world, world.rand, new BlockCoord(x, y, z), 0);
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args)
    {
        if (args.length == 1)
        {
            Set<String> allStructureNames = StructureHandler.getAllStructureNames();

            return getListOfStringsMatchingLastWord(args, allStructureNames.toArray(new String[allStructureNames.size()]));
        }
        else if (args.length == 2 || args.length == 3 || args.length == 4)
        {
            return getListOfStringsMatchingLastWord(args, "~");
        }

        return null;
    }
}
