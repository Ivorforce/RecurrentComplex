/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.reccomplex.entities.StructureEntityInfo;
import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Created by lukas on 25.05.14.
 */
public abstract class CommandSelectModify extends CommandBase
{
    @Override
    public void processCommand(ICommandSender commandSender, String[] args)
    {
        EntityPlayerMP entityPlayerMP = getCommandSenderAsPlayer(commandSender);
        StructureEntityInfo structureEntityInfo = RCCommands.getStructureEntityInfo(entityPlayerMP);

        if (structureEntityInfo != null)
        {
            if (structureEntityInfo.hasValidSelection())
            {
                processCommandSelection(entityPlayerMP, structureEntityInfo, structureEntityInfo.selectedPoint1, structureEntityInfo.selectedPoint2, args);
            }
            else
            {
                throw new CommandException("commands.selectModify.noSelection");
            }
        }
    }

    public static int[] getMetadatas(String arg)
    {
        try
        {
            String[] strings = arg.split(",");
            int[] ints = new int[strings.length];

            for (int i = 0; i < strings.length; i++)
            {
                ints[i] = Integer.valueOf(strings[i]);
            }

            return ints;
        }
        catch (Exception ex)
        {
            throw new WrongUsageException("commands.selectModify.invalidMetadata", arg);
        }
    }

    public abstract void processCommandSelection(EntityPlayerMP player, StructureEntityInfo structureEntityInfo, BlockCoord point1, BlockCoord point2, String[] args);
}
