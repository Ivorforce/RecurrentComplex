/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import com.google.common.collect.Lists;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import joptsimple.internal.Strings;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
        args = parseQuotedWords(args);

        if (args.length < 1)
        {
            throw ServerTranslations.wrongUsageException("commands.strucImportSchematic.usage");
        }

        String schematicName = args[0];
        if (schematicName.indexOf("\"") == 0)
            schematicName = schematicName.substring(1, schematicName.length() - 1);
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

        BlockPos pos = RCCommands.tryParseBlockPos(commandSender, args, 1, false);
        AxisAlignedTransform2D transform = RCCommands.tryParseTransform(args, 4);

        OperationRegistry.queueOperation(new OperationGenerateSchematic(schematicFile, transform, pos), commandSender);
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        args = parseQuotedWords(args);

        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, SchematicLoader.currentSchematicFileNames()
            .stream().map(name -> name.contains(" ") ? String.format("\"%s\"", name) : name).collect(Collectors.toList()));
        else if (args.length == 2 || args.length == 3 || args.length == 4)
            return getTabCompletionCoordinate(args, args.length - 1, pos);
        else if (args.length == 5 || args.length == 6)
            return RCCommands.completeTransform(args, args.length - 5);

        return Collections.emptyList();
    }

    public static String[] parseQuotedWords(String[] args)
    {
        List<String> list = Lists.newArrayList();

        int lastQuote = -1;
        for (int i = 0; i < args.length; i++)
        {
            if (lastQuote == -1 && args[i].indexOf("\"") == 0)
                lastQuote = i;

            if (lastQuote == -1)
                list.add(args[i]);
            else if (lastQuote >= 0 && args[i].lastIndexOf("\"") == args[i].length() -1)
            {
                list.add(Strings.join(Arrays.asList(args).subList(lastQuote, i + 1), " "));
                lastQuote = -1;
            }
        }

        if (lastQuote >= 0)
            list.add(Strings.join(Arrays.asList(args).subList(lastQuote, args.length), " "));

        return list.toArray(new String[list.size()]);
    }
}
