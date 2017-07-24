/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.schematic;

import ivorius.mcopts.commands.CommandExpecting;
import ivorius.mcopts.commands.parameters.Parameters;
import ivorius.mcopts.commands.parameters.expect.Expect;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.commands.parameters.RCP;
import ivorius.reccomplex.commands.parameters.expect.RCE;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.network.PacketEditStructureHandler;
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
    public String getName()
    {
        return "convert";
    }

    @Override
    public void expect(Expect expect)
    {
        expect
                .then(RCE::schematic)
                .named("id").then(RCE::randomString).descriptionU("export id")
                .named("from").then(RCE::structure)
                .named("directory", "d").then(RCE::resourceDirectory)
        ;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        Parameters parameters = Parameters.of(args, expect()::declare);

        EntityPlayerMP player = getCommandSenderAsPlayer(commandSender);

        if (args.length < 1)
            throw RecurrentComplex.translations.wrongUsageException("commands.rcconvertschematic.usage");

        ResourceDirectory directory = parameters.get("directory").to(RCP::resourceDirectory).optional().orElse(null);

        String schematicName = parameters.get(0).require();
        String structureID = parameters.get("id").optional().orElse(schematicName);
        SchematicFile schematicFile = CommandImportSchematic.parseSchematic(schematicName);

        GenericStructure from = parameters.get("from").to(RCP::structureFromBlueprint, commandSender).require();

        from.worldDataCompound = CommandExportSchematic.toWorldData(schematicFile).createTagCompound();

        PacketEditStructureHandler.openEditStructure(player, from, player.getPosition(), structureID, directory);
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 4;
    }
}
