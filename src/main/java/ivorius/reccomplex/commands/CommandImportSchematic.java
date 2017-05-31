/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.commands.parameters.Parameters;
import ivorius.reccomplex.operation.OperationRegistry;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.structure.schematics.OperationGenerateSchematic;
import ivorius.reccomplex.world.gen.feature.structure.schematics.SchematicFile;
import ivorius.reccomplex.world.gen.feature.structure.schematics.SchematicLoader;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandImportSchematic extends CommandBase
{
    @Nonnull
    protected static SchematicFile parseSchematic(String schematicName) throws CommandException
    {
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

        return schematicFile;
    }

    @Nonnull
    public static String trimQuotes(String arg)
    {
        String schematicName = arg;
        if (schematicName.indexOf("\"") == 0)
            schematicName = schematicName.substring(1, schematicName.length() - 1);
        return schematicName;
    }

    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "importschematic";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.strucImportSchematic.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        args = Parameters.quoted(args);

        if (args.length < 1)
            throw ServerTranslations.wrongUsageException("commands.strucImportSchematic.usage");

        SchematicFile schematicFile = parseSchematic(trimQuotes(args[0]));

        BlockPos pos = RCCommands.tryParseBlockPos(commandSender, args, 1, false);
        AxisAlignedTransform2D transform = RCCommands.tryParseTransform(args, 4);

        OperationRegistry.queueOperation(new OperationGenerateSchematic(schematicFile, transform, pos), commandSender);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        args = Parameters.quoted(args);

        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, SchematicLoader.currentSchematicFileNames()
                    .stream().map(name -> name.contains(" ") ? String.format("\"%s\"", name) : name).collect(Collectors.toList()));
        else if (args.length == 2 || args.length == 3 || args.length == 4)
            return getTabCompletionCoordinate(args, args.length - 1, pos);
        else if (args.length == 5 || args.length == 6)
            return RCCommands.completeTransform(args, args.length - 5);

        return Collections.emptyList();
    }
}
