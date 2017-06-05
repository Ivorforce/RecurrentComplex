/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.schematic;

import ivorius.reccomplex.commands.parameters.Expect;
import ivorius.reccomplex.commands.parameters.RCExpect;
import ivorius.reccomplex.commands.parameters.RCParameters;
import ivorius.reccomplex.commands.parameters.CommandExpecting;
import ivorius.reccomplex.commands.structure.CommandExportStructure;
import ivorius.reccomplex.network.PacketEditStructureHandler;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import ivorius.reccomplex.world.gen.feature.structure.schematics.SchematicFile;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandConvertSchematic extends CommandExpecting
{
    @Override
    public String getCommandName()
    {
        return "convert";
    }

    @Override
    public Expect<?> expect()
    {
        return RCExpect.expectRC()
                .schematic()
                .named("from").structure();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args, expect()::declare);

        EntityPlayerMP player = getCommandSenderAsPlayer(commandSender);

        if (args.length < 1)
            throw ServerTranslations.wrongUsageException("commands.rcconvertschematic.usage");

        String schematicName = parameters.get().first().require();
        SchematicFile schematicFile = CommandImportSchematic.parseSchematic(schematicName);

        GenericStructure structure = CommandExportStructure.getNewGenericStructure(commandSender, parameters.rc("from"));

        structure.worldDataCompound = CommandExportSchematic.toWorldData(schematicFile).createTagCompound();

        PacketEditStructureHandler.openEditStructure(structure, schematicName, player);
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 4;
    }
}
