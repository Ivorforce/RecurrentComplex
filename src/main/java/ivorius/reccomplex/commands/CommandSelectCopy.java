/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import net.minecraft.util.BlockPos;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentTranslation;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectCopy extends CommandSelectModify
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "copy";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.selectCopy.usage");
    }

    @Override
    public void processCommandSelection(EntityPlayerMP player, StructureEntityInfo structureEntityInfo, BlockPos point1, BlockPos point2, String[] args)
    {
        BlockArea area = new BlockArea(point1, point2);
        IvWorldData worldData = new IvWorldData(player.worldObj, area, true);

        BlockPos lowerCorner = area.getLowerCorner();
        BlockPos higherCorner = area.getHigherCorner();

        structureEntityInfo.setWorldDataClipboard(worldData.createTagCompound(lowerCorner));
        player.addChatMessage(ServerTranslations.format("commands.selectCopy.success", String.valueOf(lowerCorner.getX()), String.valueOf(lowerCorner.getY()), String.valueOf(lowerCorner.getZ()), String.valueOf(higherCorner.getX()), String.valueOf(higherCorner.getY()), String.valueOf(higherCorner.getZ())));
    }
}
