/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.clipboard;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.world.MockWorld;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.commands.CommandVirtual;
import ivorius.reccomplex.commands.RCCommands;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.capability.RCEntityInfo;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.ICommandSender;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectCopy extends CommandVirtual
{
    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "copy";
    }

    @Override
    public String getUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.selectCopy.usage");
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MockWorld world, ICommandSender commandSender, String[] args) throws CommandException
    {
        RCEntityInfo RCEntityInfo = RCCommands.getStructureEntityInfo(commandSender, null);

        SelectionOwner selectionOwner = RCCommands.getSelectionOwner(commandSender, null, true);
        RCCommands.assertSize(commandSender, selectionOwner);
        BlockArea area = selectionOwner.getSelection();

        IvWorldData worldData = IvWorldData.capture(world, area, true);

        BlockPos lowerCorner = area.getLowerCorner();
        BlockPos higherCorner = area.getHigherCorner();

        RCEntityInfo.setWorldDataClipboard(worldData.createTagCompound());
        commandSender.sendMessage(ServerTranslations.format("commands.selectCopy.success", String.valueOf(lowerCorner.getX()), String.valueOf(lowerCorner.getY()), String.valueOf(lowerCorner.getZ()), String.valueOf(higherCorner.getX()), String.valueOf(higherCorner.getY()), String.valueOf(higherCorner.getZ())));
    }
}
