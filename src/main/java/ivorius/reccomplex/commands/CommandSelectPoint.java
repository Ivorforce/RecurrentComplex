/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockCoord;
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
public class CommandSelectPoint extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "selectSet";
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

        if (entityPlayerMP != null)
        {
            StructureEntityInfo structureEntityInfo = StructureEntityInfo.getStructureEntityInfo(entityPlayerMP);

            if (structureEntityInfo != null)
            {
                if (args.length >= 1)
                {
                    switch (args[0])
                    {
                        case "clear":
                            structureEntityInfo.selectedPoint1 = null;
                            structureEntityInfo.selectedPoint2 = null;
                            structureEntityInfo.sendSelectionChangesToClients(entityPlayerMP);
                            break;
                        case "point1":
                        case "point2":
                            if (args.length >= 4)
                            {
                                int x = commandSender.getPlayerCoordinates().posX;
                                int y = commandSender.getPlayerCoordinates().posY;
                                int z = commandSender.getPlayerCoordinates().posZ;
                                x = MathHelper.floor_double(func_110666_a(commandSender, (double) x, args[1]));
                                y = MathHelper.floor_double(func_110666_a(commandSender, (double) y, args[2]));
                                z = MathHelper.floor_double(func_110666_a(commandSender, (double) z, args[3]));

                                if ("point1".equals(args[0]))
                                {
                                    structureEntityInfo.selectedPoint1 = new BlockCoord(x, y, z);
                                }
                                else
                                {
                                    structureEntityInfo.selectedPoint2 = new BlockCoord(x, y, z);
                                }

                                structureEntityInfo.sendSelectionChangesToClients(entityPlayerMP);
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
        }
        else
        {
            throw new WrongUsageException("commands.selectSet.noPlayer");
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args)
    {
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, "clear", "point1", "point2");
        }
        else if (args.length == 2 || args.length == 3 || args.length == 4)
        {
            return getListOfStringsMatchingLastWord(args, "~");
        }

        return null;
    }
}
