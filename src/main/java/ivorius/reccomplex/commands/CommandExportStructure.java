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
    public static GenericStructureInfo getGenericStructureInfo(String name) throws CommandException
    {
        StructureInfo structureInfo = StructureRegistry.INSTANCE.get(name);

        if (structureInfo == null)
            throw ServerTranslations.commandException("commands.structure.notRegistered", name);

        GenericStructureInfo genericStructureInfo = structureInfo.copyAsGenericStructureInfo();

        if (genericStructureInfo == null)
            throw ServerTranslations.commandException("commands.structure.notGeneric", name);

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
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, StructureRegistry.INSTANCE.ids());

        return Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        EntityPlayerMP player = getCommandSenderAsPlayer(commandSender);

        GenericStructureInfo genericStructureInfo;
        String structureID;

        if (args.length >= 1)
        {
            genericStructureInfo = getGenericStructureInfo(args[0]);
            structureID = args[0];
        }
        else
        {
            genericStructureInfo = GenericStructureInfo.createDefaultStructure();
            structureID = "NewStructure";
            genericStructureInfo.metadata.authors = commandSender.getName();
        }

        SelectionOwner selectionOwner = RCCommands.getSelectionOwner(commandSender, null, true);
        RCCommands.assertSize(commandSender, selectionOwner);

        BlockArea area = selectionOwner.getSelection();
        BlockPos lowerCoord = area.getLowerCorner();
        BlockPos higherCoord = area.getHigherCorner();

        IvWorldData data = IvWorldData.capture(commandSender.getEntityWorld(), new BlockArea(lowerCoord, higherCoord), true);
        genericStructureInfo.worldDataCompound = data.createTagCompound(lowerCoord);
        PacketEditStructureHandler.openEditStructure(genericStructureInfo, structureID, player);
    }
}
