/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import net.minecraft.command.CommandException;
import net.minecraft.util.math.BlockPos;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.ivtoolkit.blocks.BlockAreas;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectShrink extends CommandSelectModify
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "shrink";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.selectShrink.usage");
    }

    @Override
    public void executeSelection(EntityPlayerMP player, StructureEntityInfo structureEntityInfo, BlockPos point1, BlockPos point2, String[] args) throws CommandException
    {
        if (args.length < 3)
            throw ServerTranslations.wrongUsageException("commands.selectShrink.usage");

        int x = parseInt(args[0]), y = parseInt(args[1]), z = parseInt(args[2]);

        BlockArea area = BlockAreas.shrink(new BlockArea(point1, point2), new BlockPos(x, y, z), new BlockPos(x, y, z));

        structureEntityInfo.setSelection(area);
        structureEntityInfo.sendSelectionToClients(player);
    }
}
