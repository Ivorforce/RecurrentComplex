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
        return "commands.selectCopy.usage";
    }

    @Override
    public void processCommandSelection(EntityPlayerMP player, StructureEntityInfo structureEntityInfo, BlockCoord point1, BlockCoord point2, String[] args)
    {
        BlockArea area = new BlockArea(point1, point2);
        IvWorldData worldData = new IvWorldData(player.worldObj, area, true);

        BlockCoord lowerCorner = area.getLowerCorner();
        BlockCoord higherCorner = area.getHigherCorner();

        structureEntityInfo.setWorldDataClipboard(worldData.createTagCompound(lowerCorner));
        player.addChatMessage(new ChatComponentTranslation("commands.selectCopy.success", String.valueOf(lowerCorner.x), String.valueOf(lowerCorner.y), String.valueOf(lowerCorner.z), String.valueOf(higherCorner.x), String.valueOf(higherCorner.y), String.valueOf(higherCorner.z)));
    }
}
