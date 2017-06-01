/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.commands.parameters.RCExpect;
import ivorius.reccomplex.commands.parameters.RCParameters;
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
import java.util.List;

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
        RCParameters parameters = RCParameters.of(args, "m");

        if (args.length < 1)
            throw ServerTranslations.wrongUsageException("commands.strucImportSchematic.usage");

        SchematicFile schematicFile = parseSchematic(parameters.get().first().require());
        BlockPos pos = parameters.mc("x").pos(parameters.get("y"), parameters.get("z"), commandSender.getPosition(), false).require();
        AxisAlignedTransform2D transform = parameters.transform("r", "m").optional().orElse(AxisAlignedTransform2D.ORIGINAL);

        OperationRegistry.queueOperation(new OperationGenerateSchematic(schematicFile, transform, pos), commandSender);
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        return RCExpect.startRC()
                .schematic()
                .named("x").x()
                .named("y").y()
                .named("z").z()
                .named("r").rotation()
                .flag("m")
                .get(server, sender, args, pos);
    }
}
