/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.structure;

import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.mcopts.commands.CommandExpecting;
import ivorius.mcopts.commands.parameters.Parameters;
import ivorius.mcopts.commands.parameters.expect.Expect;
import ivorius.reccomplex.commands.parameters.RCP;
import ivorius.reccomplex.commands.parameters.expect.RCE;
import ivorius.reccomplex.network.PacketEditStructureHandler;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandExportStructure extends CommandExpecting
{
    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "export";
    }

    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    @Override
    public void expect(Expect expect)
    {
        expect
                .then(RCE::structure).descriptionU("copy structure")
                .named("id").then(RCE::randomString).descriptionU("export id")
        ;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        Parameters parameters = Parameters.of(args, expect()::declare);
        EntityPlayerMP player = getCommandSenderAsPlayer(commandSender);

        String structureID = parameters.get("id").optional().orElse(parameters.get(0).optional().orElse(null));
        GenericStructure from = parameters.get(0).to(RCP::structureFromBlueprint, commandSender).require();

        SelectionOwner selectionOwner = RCCommands.getSelectionOwner(commandSender, null, true);
        RCCommands.assertSize(commandSender, selectionOwner);

        from.worldDataCompound = IvWorldData.capture(commandSender.getEntityWorld(), selectionOwner.getSelection(), true)
                .createTagCompound();

        PacketEditStructureHandler.openEditStructure(from, structureID, player);
    }
}
