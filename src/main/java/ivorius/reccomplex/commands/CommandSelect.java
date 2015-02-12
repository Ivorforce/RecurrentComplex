/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.entities.StructureEntityInfo;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
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

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return "commands.selectSet.usage";
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args)
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
                case "both":
                case "point1":
                case "point2":
                    if (args.length >= 4)
                    {
                        if (!args[0].equals("point2"))
                        {
                            structureEntityInfo.selectedPoint1 = new BlockCoord(
                                    MathHelper.floor_double(func_110666_a(commandSender, structureEntityInfo.selectedPoint1.x, args[1])),
                                    MathHelper.floor_double(func_110666_a(commandSender, structureEntityInfo.selectedPoint1.y, args[2])),
                                    MathHelper.floor_double(func_110666_a(commandSender, structureEntityInfo.selectedPoint1.z, args[3]))
                            );
                        }
                        if (!args[0].equals("point1"))
                        {
                            structureEntityInfo.selectedPoint2 = new BlockCoord(
                                    MathHelper.floor_double(func_110666_a(commandSender, structureEntityInfo.selectedPoint2.x, args[1])),
                                    MathHelper.floor_double(func_110666_a(commandSender, structureEntityInfo.selectedPoint2.y, args[2])),
                                    MathHelper.floor_double(func_110666_a(commandSender, structureEntityInfo.selectedPoint2.z, args[3]))
                            );
                        }

                        structureEntityInfo.sendSelectionToClients(entityPlayerMP);
                    }
                    else
                    {
                        throw new WrongUsageException("commands.selectSet.usage");
                    }
                    break;
                default:
                    throw new WrongUsageException("commands.selectSet.usage");
            }
        }
        else
        {
            throw new WrongUsageException("commands.selectSet.usage");
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args)
    {
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, "both", "clear", "point1", "point2");
        }
        else if (args.length == 2 || args.length == 3 || args.length == 4)
        {
            return getListOfStringsMatchingLastWord(args, "~");
        }

        return null;
    }
}
