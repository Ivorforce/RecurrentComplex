/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.utils.BlockAreas;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectExpand extends CommandSelectModify
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "expand";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return "commands.selectExpand.usage";
    }

    @Override
    public void processCommandSelection(EntityPlayerMP player, StructureEntityInfo structureEntityInfo, BlockCoord point1, BlockCoord point2, String[] args)
    {
        if (args.length < 3)
            throw new WrongUsageException("commands.selectExpand.usage");

        int x = parseInt(player, args[0]), y = parseInt(player, args[1]), z = parseInt(player, args[2]);

        BlockArea area = BlockAreas.expand(new BlockArea(point1, point2), new BlockCoord(x, y, z), new BlockCoord(x, y, z));

        structureEntityInfo.setSelection(area);
        structureEntityInfo.sendSelectionToClients(player);
    }
}
