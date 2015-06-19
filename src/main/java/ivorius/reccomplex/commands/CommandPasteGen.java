/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockCoord;
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
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandPasteGen extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "pastegen";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.strucPasteGen.usage");
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args)
    {
        int x, y, z;

        EntityPlayerMP entityPlayerMP = getCommandSenderAsPlayer(commandSender);
        StructureEntityInfo structureEntityInfo = RCCommands.getStructureEntityInfo(entityPlayerMP);

        NBTTagCompound worldData = structureEntityInfo.getWorldDataClipboard();

        if (worldData != null)
        {
            World world = commandSender.getEntityWorld();

            x = commandSender.getPlayerCoordinates().posX;
            y = commandSender.getPlayerCoordinates().posY;
            z = commandSender.getPlayerCoordinates().posZ;

            if (args.length >= 3)
            {
                x = MathHelper.floor_double(func_110666_a(commandSender, (double) x, args[0]));
                y = MathHelper.floor_double(func_110666_a(commandSender, (double) y, args[1]));
                z = MathHelper.floor_double(func_110666_a(commandSender, (double) z, args[2]));
            }

            int rotation = args.length >= 4 ? parseInt(commandSender, args[3]) : 0;
            boolean mirror = args.length >= 5 && parseBoolean(commandSender, args[4]);

            GenericStructureInfo structureInfo = GenericStructureInfo.createDefaultStructure();
            structureInfo.worldDataCompound = worldData;

            BlockCoord coord = new BlockCoord(x, y, z);
            AxisAlignedTransform2D transform = AxisAlignedTransform2D.from(rotation, mirror);

            OperationRegistry.queueOperation(new OperationGenerateStructure(structureInfo, transform, coord, false), commandSender);
        }
        else
        {
            throw ServerTranslations.commandException("commands.strucPaste.noClipboard");
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args)
    {
        if (args.length == 1 || args.length == 2 || args.length == 3)
        {
            return getListOfStringsMatchingLastWord(args, "~");
        }

        return null;
    }
}
