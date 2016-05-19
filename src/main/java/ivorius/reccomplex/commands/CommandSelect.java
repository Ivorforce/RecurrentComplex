/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import net.minecraft.command.CommandException;
import net.minecraft.util.BlockPos;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MathHelper;

import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandSelect extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "select";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.selectSet.usage");
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args) throws CommandException
    {
        EntityPlayerMP entityPlayerMP = getCommandSenderAsPlayer(commandSender);
        StructureEntityInfo structureEntityInfo = RCCommands.getStructureEntityInfo(entityPlayerMP);

        if (args.length >= 1)
        {
            switch (args[0])
            {
                case "clear":
                    structureEntityInfo.selectedPoint1 = null;
                    structureEntityInfo.selectedPoint2 = null;
                    structureEntityInfo.sendSelectionToClients(entityPlayerMP);
                    break;
                case "get":
                    commandSender.addChatMessage(ServerTranslations.format("commands.selectSet.get", translatePoint(structureEntityInfo.selectedPoint1), translatePoint(structureEntityInfo.selectedPoint2)));
                    break;
                case "both":
                case "point1":
                case "point2":
                    if (args.length >= 4)
                    {
                        if (!args[0].equals("point2"))
                        {
                            if (structureEntityInfo.selectedPoint1 == null)
                                structureEntityInfo.selectedPoint1 = new BlockPos(MathHelper.floor_double(entityPlayerMP.posX), MathHelper.floor_double(entityPlayerMP.posY), MathHelper.floor_double(entityPlayerMP.posZ));

                            structureEntityInfo.selectedPoint1 = RCCommands.parseBlockPos(structureEntityInfo.selectedPoint1, args, 1, false);
                        }
                        if (!args[0].equals("point1"))
                        {
                            if (structureEntityInfo.selectedPoint2 == null)
                                structureEntityInfo.selectedPoint2 = new BlockPos(MathHelper.floor_double(entityPlayerMP.posX), MathHelper.floor_double(entityPlayerMP.posY), MathHelper.floor_double(entityPlayerMP.posZ));

                            structureEntityInfo.selectedPoint2 = RCCommands.parseBlockPos(structureEntityInfo.selectedPoint2, args, 1, false);
                        }

                        structureEntityInfo.sendSelectionToClients(entityPlayerMP);
                    }
                    else
                    {
                        throw ServerTranslations.wrongUsageException("commands.selectSet.usage");
                    }
                    break;
                default:
                    throw ServerTranslations.wrongUsageException("commands.selectSet.usage");
            }
        }
        else
        {
            throw ServerTranslations.wrongUsageException("commands.selectSet.usage");
        }
    }

    protected Object translatePoint(BlockPos coord)
    {
        return coord != null
                ? String.format("[%d,%d,%d]", coord.getX(), coord.getY(), coord.getZ())
                : ServerTranslations.format("commands.selectSet.point.none");
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, "both", "clear", "point1", "point2", "get");
        }
        else if (args.length == 2 || args.length == 3 || args.length == 4)
        {
            return getListOfStringsMatchingLastWord(args, "~");
        }

        return null;
    }
}
