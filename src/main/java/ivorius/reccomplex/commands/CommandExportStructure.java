/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.commands.parameters.RCExpect;
import ivorius.reccomplex.commands.parameters.RCParameter;
import ivorius.reccomplex.commands.parameters.RCParameters;
import ivorius.reccomplex.network.PacketEditStructureHandler;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandExportStructure extends CommandBase
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
    public String getUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.strucExport.usage");
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        return RCExpect.startRC()
                .named("from").structure()
                .get(server, sender, args, pos);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args);
        EntityPlayerMP player = getCommandSenderAsPlayer(commandSender);

        String structureID = parameters.get().first().optional().orElse(null);
        GenericStructure genericStructureInfo = getNewGenericStructure(commandSender, parameters.rc("from"));

        SelectionOwner selectionOwner = RCCommands.getSelectionOwner(commandSender, null, true);
        RCCommands.assertSize(commandSender, selectionOwner);

        genericStructureInfo.worldDataCompound = IvWorldData.capture(commandSender.getEntityWorld(), selectionOwner.getSelection(), true)
                .createTagCompound();

        PacketEditStructureHandler.openEditStructure(genericStructureInfo, structureID, player);
    }
}
