/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import net.minecraft.command.CommandException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
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
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        EntityPlayerMP entityPlayerMP = getCommandSenderAsPlayer(commandSender);
        StructureEntityInfo seInfo = RCCommands.getStructureEntityInfo(entityPlayerMP);

        if (args.length >= 1)
        {
            switch (args[0])
            {
                case "clear":
                    seInfo.selectedPoint1 = null;
                    seInfo.selectedPoint2 = null;
                    seInfo.sendSelectionToClients(entityPlayerMP);
                    break;
                case "get":
                    commandSender.addChatMessage(ServerTranslations.format("commands.selectSet.get", translatePoint(seInfo.selectedPoint1), translatePoint(seInfo.selectedPoint2), translatePoint(seInfo.selectedPoint2)));
                    if (seInfo.hasValidSelection())
                        commandSender.addChatMessage(ServerTranslations.format("commands.selectSet.size", translateSize(new BlockArea(seInfo.selectedPoint1, seInfo.selectedPoint2).areaSize())));
                    break;
                case "both":
                case "point1":
                case "point2":
                    if (args.length >= 4)
                    {
                        if (!args[0].equals("point2"))
                        {
                            if (seInfo.selectedPoint1 == null)
                                seInfo.selectedPoint1 = new BlockPos(MathHelper.floor_double(entityPlayerMP.posX), MathHelper.floor_double(entityPlayerMP.posY), MathHelper.floor_double(entityPlayerMP.posZ));

                            seInfo.selectedPoint1 = RCCommands.parseBlockPos(seInfo.selectedPoint1, args, 1, false);
                        }
                        if (!args[0].equals("point1"))
                        {
                            if (seInfo.selectedPoint2 == null)
                                seInfo.selectedPoint2 = new BlockPos(MathHelper.floor_double(entityPlayerMP.posX), MathHelper.floor_double(entityPlayerMP.posY), MathHelper.floor_double(entityPlayerMP.posZ));

                            seInfo.selectedPoint2 = RCCommands.parseBlockPos(seInfo.selectedPoint2, args, 1, false);
                        }

                        seInfo.sendSelectionToClients(entityPlayerMP);
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

    protected Object translateSize(int[] size)
    {
        return size != null
                ? String.format("[%d,%d,%d]", size[0], size[1], size[2])
                : ServerTranslations.format("commands.selectSet.point.none");
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, "both", "clear", "point1", "point2", "get");
        else if (args.length == 2 || args.length == 3 || args.length == 4)
            return getTabCompletionCoordinate(args, args.length - 1, pos);

        return super.getTabCompletionOptions(server, sender, args, pos);
    }
}
