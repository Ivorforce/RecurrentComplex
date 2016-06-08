/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import net.minecraft.command.*;
import net.minecraft.util.BlockPos;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Created by lukas on 25.05.14.
 */
public abstract class CommandSelectModify extends CommandBase
{
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args) throws CommandException
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
                throw ServerTranslations.commandException("commands.selectModify.noSelection");
            }
        }
    }

    public static int[] getMetadatas(String arg) throws CommandException
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
            throw ServerTranslations.wrongUsageException("commands.selectModify.invalidMetadata", arg);
        }
    }

    public abstract void processCommandSelection(EntityPlayerMP player, StructureEntityInfo structureEntityInfo, BlockPos point1, BlockPos point2, String[] args) throws CommandException;
}
