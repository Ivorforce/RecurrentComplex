/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.schematic;

import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.commands.parameters.Expect;
import ivorius.reccomplex.commands.parameters.RCExpect;
import ivorius.reccomplex.commands.parameters.RCParameters;
import ivorius.reccomplex.commands.parameters.CommandExpecting;
import ivorius.reccomplex.operation.OperationRegistry;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.structure.schematics.OperationGenerateSchematic;
import ivorius.reccomplex.world.gen.feature.structure.schematics.SchematicFile;
import ivorius.reccomplex.world.gen.feature.structure.schematics.SchematicLoader;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandImportSchematic extends CommandExpecting
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
            throw ServerTranslations.commandException("commands.rcimportschematic.format", schematicName, e.format);
        }

        if (schematicFile == null)
            throw ServerTranslations.commandException("commands.rcimportschematic.missing", schematicName, SchematicLoader.getLookupFolderName());

        return schematicFile;
    }

    @Override
    public String getCommandName()
    {
        return "import";
    }

    @Override
    public Expect<?> expect()
    {
        return RCExpect.expectRC()
                .schematic()
                .pos("x", "y", "z")
                .named("rotation", "r").rotation()
                .flag("mirror", "m");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args, p -> p
                .flag("mirror", "m")
        );

        if (args.length < 1)
            throw ServerTranslations.wrongUsageException("commands.rcimportschematic.usage");

        SchematicFile schematicFile = parseSchematic(parameters.get().first().require());
        BlockPos pos = parameters.pos("x", "y", "z", commandSender.getPosition(), false).require();
        AxisAlignedTransform2D transform = parameters.transform("rotation", "mirror").optional().orElse(AxisAlignedTransform2D.ORIGINAL);

        OperationRegistry.queueOperation(new OperationGenerateSchematic(schematicFile, transform, pos), commandSender);
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 4;
    }
}
