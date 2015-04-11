/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockAreas;
import ivorius.ivtoolkit.blocks.BlockCoord;
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
    public void processCommandSelection(EntityPlayerMP player, StructureEntityInfo structureEntityInfo, BlockCoord point1, BlockCoord point2, String[] args)
    {
        if (args.length < 3)
            throw ServerTranslations.wrongUsageException("commands.selectShift.usage");

        int x = parseInt(player, args[0]), y = parseInt(player, args[1]), z = parseInt(player, args[2]);

        structureEntityInfo.selectedPoint1 = point1.add(x, y, z);
        structureEntityInfo.selectedPoint2 = point2.add(x, y, z);
        structureEntityInfo.sendSelectionToClients(player);
    }
}
