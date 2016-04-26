/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.network.PacketEditStructureHandler;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.StructureInfo;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandExportStructure extends CommandBase
{
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
    public void processCommand(ICommandSender commandSender, String[] args)
    {
        EntityPlayerMP player = getCommandSenderAsPlayer(commandSender);

        BlockArea area;

//        if (args.length >= 6)
//        {
//            x = commandSender.getPlayerCoordinates().posX;
//            y = commandSender.getPlayerCoordinates().posY;
//            z = commandSender.getPlayerCoordinates().posZ;
//            x = MathHelper.floor_double(func_110666_a(commandSender, (double) x, args[0]));
//            y = MathHelper.floor_double(func_110666_a(commandSender, (double) y, args[1]));
//            z = MathHelper.floor_double(func_110666_a(commandSender, (double) z, args[2]));
//
//            width = Integer.valueOf(args[3]);
//            height = Integer.valueOf(args[4]);
//            length = Integer.valueOf(args[5]);
//        }
//        else
        {
            StructureEntityInfo structureEntityInfo = RCCommands.getStructureEntityInfo(player);

            if (structureEntityInfo.hasValidSelection())
            {
                area = new BlockArea(structureEntityInfo.selectedPoint1, structureEntityInfo.selectedPoint2);
            }
            else
            {
                throw ServerTranslations.wrongUsageException("commands.selectModify.noSelection");
            }
        }

        GenericStructureInfo genericStructureInfo;
        String structureID;
        boolean saveAsActive;

        if (args.length >= 1)
        {
            genericStructureInfo = getGenericStructureInfo(args[0]);
            structureID = args[0];
            saveAsActive = StructureRegistry.INSTANCE.isStructureGenerating(structureID);
        }
        else
        {
            genericStructureInfo = GenericStructureInfo.createDefaultStructure();
            structureID = "NewStructure";
            genericStructureInfo.metadata.authors = commandSender.getCommandSenderName();
            saveAsActive = false;
        }

        BlockCoord lowerCoord = area.getLowerCorner();
        BlockCoord higherCoord = area.getHigherCorner();

        IvWorldData data = new IvWorldData(player.getEntityWorld(), new BlockArea(lowerCoord, higherCoord), true);
        genericStructureInfo.worldDataCompound = data.createTagCompound(lowerCoord);
        PacketEditStructureHandler.openEditStructure(genericStructureInfo, structureID, saveAsActive, player);
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args)
    {
        if (args.length == 1)
            return getListOfStringsFromIterableMatchingLastWord(args, StructureRegistry.INSTANCE.allStructureIDs());

        return null;
    }

    public static GenericStructureInfo getGenericStructureInfo(String name)
    {
        StructureInfo structureInfo = StructureRegistry.INSTANCE.getStructure(name);

        if (structureInfo == null)
            throw ServerTranslations.commandException("commands.structure.notRegistered", name);

        GenericStructureInfo genericStructureInfo = structureInfo.copyAsGenericStructureInfo();

        if (genericStructureInfo == null)
            throw ServerTranslations.commandException("commands.structure.notGeneric", name);

        return genericStructureInfo;
    }
}
