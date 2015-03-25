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
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.List;
import java.util.Set;

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

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return "commands.strucExport.usage";
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args)
    {
        EntityPlayerMP player = getCommandSenderAsPlayer(commandSender);

        int x, y, z;
        int width, height, length;

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
                BlockCoord smaller = structureEntityInfo.selectedPoint1.getLowerCorner(structureEntityInfo.selectedPoint2);
                BlockCoord bigger = structureEntityInfo.selectedPoint1.getHigherCorner(structureEntityInfo.selectedPoint2);
                x = smaller.x;
                y = smaller.y;
                z = smaller.z;
                width = bigger.x - smaller.x + 1;
                height = bigger.y - smaller.y + 1;
                length = bigger.z - smaller.z + 1;
            }
            else
            {
                throw new WrongUsageException("commands.selectModify.noSelection");
            }
        }

        GenericStructureInfo genericStructureInfo;
        String structureName;

        if (args.length >= 1)
        {
            genericStructureInfo = getGenericStructureInfo(args[0]);
            structureName = args[0];
        }
        else
        {
            genericStructureInfo = GenericStructureInfo.createDefaultStructure();
            structureName = "NewStructure";
        }

        BlockCoord lowerCoord = new BlockCoord(x, y, z);
        BlockCoord higherCoord = new BlockCoord(x + width - 1, y + height - 1, z + length - 1);

        IvWorldData data = new IvWorldData(player.getEntityWorld(), new BlockArea(lowerCoord, higherCoord), true);
        genericStructureInfo.worldDataCompound = data.createTagCompound(lowerCoord);
        PacketEditStructureHandler.sendEditStructure(genericStructureInfo, structureName, player);
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args)
    {
        if (args.length == 1)
            return getListOfStringsFromIterableMatchingLastWord(args, StructureRegistry.getAllStructureNames());

        return null;
    }

    public static GenericStructureInfo getGenericStructureInfo(String name)
    {
        StructureInfo structureInfo = StructureRegistry.getStructure(name);

        if (structureInfo == null)
            throw new CommandException("commands.structure.notRegistered", name);

        GenericStructureInfo genericStructureInfo = structureInfo.copyAsGenericStructureInfo();

        if (genericStructureInfo == null)
            throw new CommandException("commands.structure.notGeneric", name);

        return genericStructureInfo;
    }
}
