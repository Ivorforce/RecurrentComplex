/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.network.PacketEditStructureHandler;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.structure.StructureInfo;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructureInfo;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandExportStructure extends CommandBase
{
    public static GenericStructureInfo getGenericStructure(String name) throws CommandException
    {
        StructureInfo structureInfo = StructureRegistry.INSTANCE.get(name);

        if (structureInfo == null)
            throw ServerTranslations.commandException("commands.structure.notRegistered", name);

        GenericStructureInfo genericStructureInfo = structureInfo.copyAsGenericStructureInfo();

        if (genericStructureInfo == null)
            throw ServerTranslations.commandException("commands.structure.notGeneric", name);

        return genericStructureInfo;
    }

    protected static GenericStructureInfo getGenericStructure(ICommandSender commandSender, String structureID) throws CommandException
    {
        GenericStructureInfo genericStructureInfo;

        if (structureID != null)
        {
            genericStructureInfo = getGenericStructure(structureID);
        }
        else
        {
            genericStructureInfo = GenericStructureInfo.createDefaultStructure();
            genericStructureInfo.metadata.authors = commandSender.getName();
        }
        return genericStructureInfo;
    }

    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "export";
    }

    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.strucExport.usage");
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, StructureRegistry.INSTANCE.ids());

        return Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        EntityPlayerMP player = getCommandSenderAsPlayer(commandSender);

        String structureID = args.length >= 1 ? args[0] : null;
        GenericStructureInfo genericStructureInfo = getGenericStructure(commandSender, structureID);

        SelectionOwner selectionOwner = RCCommands.getSelectionOwner(commandSender, null, true);
        RCCommands.assertSize(commandSender, selectionOwner);

        genericStructureInfo.worldDataCompound = IvWorldData.capture(commandSender.getEntityWorld(), selectionOwner.getSelection(), true)
                .createTagCompound();

        PacketEditStructureHandler.openEditStructure(genericStructureInfo, structureID, player);
    }
}
