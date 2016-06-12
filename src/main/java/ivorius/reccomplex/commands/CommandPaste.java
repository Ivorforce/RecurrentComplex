/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import net.minecraft.util.BlockPos;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.operation.OperationRegistry;
import ivorius.reccomplex.structures.OperationGenerateStructure;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandPaste extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "paste";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.strucPaste.usage");
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args) throws CommandException
    {
        EntityPlayerMP entityPlayerMP = getCommandSenderAsPlayer(commandSender);
        StructureEntityInfo structureEntityInfo = RCCommands.getStructureEntityInfo(entityPlayerMP);

        NBTTagCompound worldData = structureEntityInfo.getWorldDataClipboard();

        if (worldData != null)
        {
            BlockPos coord;

            if (args.length >= 3)
                coord = parseBlockPos(commandSender, args, 0, false);
            else
                coord = commandSender.getPosition();

            int rotation = args.length >= 4 ? parseInt(args[3]) : 0;
            boolean mirror = args.length >= 5 && parseBoolean(args[4]);

            GenericStructureInfo structureInfo = GenericStructureInfo.createDefaultStructure();
            structureInfo.worldDataCompound = worldData;

            AxisAlignedTransform2D transform = AxisAlignedTransform2D.from(rotation, mirror);

            OperationRegistry.queueOperation(new OperationGenerateStructure(structureInfo, transform, coord, true), commandSender);
        }
        else
        {
            throw ServerTranslations.commandException("commands.strucPaste.noClipboard");
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args, BlockPos pos)
    {
        if (args.length == 1 || args.length == 2 || args.length == 3)
            return getListOfStringsMatchingLastWord(args, "~");
        else if (args.length == 4)
            return getListOfStringsMatchingLastWord(args, "0", "1", "2", "3");
        else if (args.length == 5)
            return getListOfStringsMatchingLastWord(args, "true", "false");

        return null;
    }
}
