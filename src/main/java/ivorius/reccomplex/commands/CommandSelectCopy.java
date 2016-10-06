/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.reccomplex.capability.SelectionOwner;
import net.minecraft.command.CommandException;
import net.minecraft.util.math.BlockPos;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.capability.StructureEntityInfo;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

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
    public void executeSelection(ICommandSender sender, SelectionOwner selectionOwner, String[] args) throws CommandException
    {
        StructureEntityInfo structureEntityInfo = RCCommands.getStructureEntityInfo(sender, null);

        BlockArea area = selectionOwner.getSelection();
        IvWorldData worldData = IvWorldData.capture(sender.getEntityWorld(), area, true);

        BlockPos lowerCorner = area.getLowerCorner();
        BlockPos higherCorner = area.getHigherCorner();

        structureEntityInfo.setWorldDataClipboard(worldData.createTagCompound(null));
        sender.addChatMessage(ServerTranslations.format("commands.selectCopy.success", String.valueOf(lowerCorner.getX()), String.valueOf(lowerCorner.getY()), String.valueOf(lowerCorner.getZ()), String.valueOf(higherCorner.getX()), String.valueOf(higherCorner.getY()), String.valueOf(higherCorner.getZ())));
    }
}
