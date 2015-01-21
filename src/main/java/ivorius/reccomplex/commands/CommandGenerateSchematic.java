/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.schematics.SchematicFile;
import ivorius.reccomplex.schematics.SchematicLoader;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandGenerateSchematic extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "strucImportSchematic";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return "commands.strucImportSchematic.usage";
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args)
    {
        int x, y, z;

        if (args.length <= 0)
        {
            throw new WrongUsageException("commands.strucImportSchematic.usage");
        }

        String schematicName = args[0];
        World world = commandSender.getEntityWorld();
        SchematicFile schematicFile = null;

        try
        {
            schematicFile = SchematicLoader.loadSchematicByName(schematicName);
        }
        catch (SchematicFile.UnsupportedSchematicFormatException e)
        {
            throw new CommandException("commands.strucImportSchematic.format", schematicName, e.format);
        }

        if (schematicFile == null)
        {
            throw new CommandException("commands.strucImportSchematic.noStructure", schematicName, SchematicLoader.getLookupFolderName());
        }

        x = commandSender.getPlayerCoordinates().posX;
        y = commandSender.getPlayerCoordinates().posY;
        z = commandSender.getPlayerCoordinates().posZ;

        if (args.length >= 4)
        {
            x = MathHelper.floor_double(func_110666_a(commandSender, (double) x, args[1]));
            y = MathHelper.floor_double(func_110666_a(commandSender, (double) y, args[2]));
            z = MathHelper.floor_double(func_110666_a(commandSender, (double) z, args[3]));
        }

        schematicFile.generate(world, x, y, z);
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args)
    {
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, SchematicLoader.currentSchematicFileNames());
        }
        else if (args.length == 2 || args.length == 3 || args.length == 4)
        {
            return getListOfStringsMatchingLastWord(args, "~");
        }

        return null;
    }
}
