/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockAreas;
import ivorius.ivtoolkit.world.MockWorld;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectExpand extends CommandVirtual
{
    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "expand";
    }

    @Override
    public String getUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.selectExpand.usage");
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    void execute(MockWorld world, ICommandSender commandSender, String[] args) throws CommandException
    {
        SelectionOwner selectionOwner = RCCommands.getSelectionOwner(commandSender, null, true);
        if (args.length < 3)
            throw ServerTranslations.wrongUsageException("commands.selectExpand.usage");

        int x = parseInt(args[0]), y = parseInt(args[1]), z = parseInt(args[2]);

        BlockArea area = BlockAreas.expand(selectionOwner.getSelection(), new BlockPos(x, y, z), new BlockPos(x, y, z));

        selectionOwner.setSelection(area);
    }
}
