/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import net.minecraft.command.CommandException;
import net.minecraft.util.math.BlockPos;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectShift extends CommandSelectModify
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "shift";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.selectShift.usage");
    }

    @Override
    public void executeSelection(EntityPlayerMP player, StructureEntityInfo structureEntityInfo, BlockPos point1, BlockPos point2, String[] args) throws CommandException
    {
        if (args.length < 3)
            throw ServerTranslations.wrongUsageException("commands.selectShift.usage");

        int x = parseInt(args[0]), y = parseInt(args[1]), z = parseInt(args[2]);

        structureEntityInfo.selectedPoint1 = point1.add(x, y, z);
        structureEntityInfo.selectedPoint2 = point2.add(x, y, z);
        structureEntityInfo.sendSelectionToClients(player);
    }
}
