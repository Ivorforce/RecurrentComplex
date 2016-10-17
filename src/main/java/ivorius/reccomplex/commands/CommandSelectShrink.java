/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.reccomplex.capability.SelectionOwner;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import ivorius.reccomplex.RCConfig;
import ivorius.ivtoolkit.blocks.BlockAreas;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.ICommandSender;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectShrink extends CommandBase
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

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        SelectionOwner selectionOwner = RCCommands.getSelectionOwner(commandSender, null, true);
        if (args.length < 3)
            throw ServerTranslations.wrongUsageException("commands.selectShrink.usage");

        int x = parseInt(args[0]), y = parseInt(args[1]), z = parseInt(args[2]);

        BlockArea area = BlockAreas.shrink(selectionOwner.getSelection(), new BlockPos(x, y, z), new BlockPos(x, y, z));

        selectionOwner.setSelection(area);
    }
}
