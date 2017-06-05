/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.structure;

import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.commands.parameters.*;
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
    public static GenericStructure getNewGenericStructure(ICommandSender commandSender, RCParameter parameter) throws CommandException
    {
        GenericStructure genericStructureInfo;

        if (parameter.has(1))
        {
            genericStructureInfo = parameter.genericStructure().require();
        }
        else
        {
            genericStructureInfo = GenericStructure.createDefaultStructure();
            genericStructureInfo.metadata.authors = commandSender.getName();
        }

        return genericStructureInfo;
    }

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
    public Expect<?> expect()
    {
        return RCExpect.expectRC()
                .randomString().description("structure id")
                .named("from").structure();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args, expect()::declare);
        EntityPlayerMP player = getCommandSenderAsPlayer(commandSender);

        String structureID = parameters.get().first().optional().orElse(null);
        GenericStructure from = getNewGenericStructure(commandSender, parameters.rc("from"));

        SelectionOwner selectionOwner = RCCommands.getSelectionOwner(commandSender, null, true);
        RCCommands.assertSize(commandSender, selectionOwner);

        from.worldDataCompound = IvWorldData.capture(commandSender.getEntityWorld(), selectionOwner.getSelection(), true)
                .createTagCompound();

        PacketEditStructureHandler.openEditStructure(from, structureID, player);
    }
}
