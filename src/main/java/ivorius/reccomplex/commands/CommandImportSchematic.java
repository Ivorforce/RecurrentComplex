/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.operation.OperationRegistry;
import ivorius.reccomplex.structures.schematics.OperationGenerateSchematic;
import ivorius.reccomplex.structures.schematics.SchematicFile;
import ivorius.reccomplex.structures.schematics.SchematicLoader;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandImportSchematic extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "importschematic";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.strucImportSchematic.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length < 4)
        {
            throw ServerTranslations.wrongUsageException("commands.strucImportSchematic.usage");
        }

        BlockPos pos = parseBlockPos(commandSender, args, 0, false);

        String schematicName = buildString(args, 3);
        SchematicFile schematicFile;

        try
        {
            schematicFile = SchematicLoader.loadSchematicByName(schematicName);
        }
        catch (SchematicFile.UnsupportedSchematicFormatException e)
        {
            throw ServerTranslations.commandException("commands.strucImportSchematic.format", schematicName, e.format);
        }

        if (schematicFile == null)
            throw ServerTranslations.commandException("commands.strucImportSchematic.missing", schematicName, SchematicLoader.getLookupFolderName());

        OperationRegistry.queueOperation(new OperationGenerateSchematic(schematicFile, pos), commandSender);
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 4)
        {
            return getListOfStringsMatchingLastWord(args, SchematicLoader.currentSchematicFileNames());
        }
        else if (args.length == 1 || args.length == 2 || args.length == 3)
        {
            return getListOfStringsMatchingLastWord(args, "~");
        }

        return null;
    }
}
