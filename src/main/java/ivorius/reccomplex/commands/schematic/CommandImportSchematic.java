/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.schematic;

import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.commands.parameters.*;
import ivorius.reccomplex.commands.rcparameters.IvP;
import ivorius.reccomplex.commands.rcparameters.RCExpect;
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
import java.util.function.Function;

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
        Function<Parameters,Parameters> c = p -> p
                .flag("mirror", "m");
        Parameters blueprint = Parameters.of(args, c);
        Parameters blueprint1 = blueprint;
        Parameters blueprint2 = blueprint1;
        Parameters parameters = new Parameters(blueprint2);

        if (args.length < 1)
            throw ServerTranslations.wrongUsageException("commands.rcimportschematic.usage");

        SchematicFile schematicFile = parseSchematic(parameters.get(0).require());
        BlockPos pos = parameters.get(MCP.pos("x", "y", "z", commandSender.getPosition(), false)).require();
        AxisAlignedTransform2D transform = parameters.get(IvP.transform("rotation", "mirror")).optional().orElse(AxisAlignedTransform2D.ORIGINAL);

        OperationRegistry.queueOperation(new OperationGenerateSchematic(schematicFile, transform, pos), commandSender);
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 4;
    }
}
