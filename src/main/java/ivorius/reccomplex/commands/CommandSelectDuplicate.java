/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import net.minecraft.command.CommandException;
import net.minecraft.util.BlockPos;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.operation.OperationRegistry;
import ivorius.reccomplex.structures.OperationGenerateStructure;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectDuplicate extends CommandSelectModify
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "duplicate";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.selectDuplicate.usage");
    }

    @Override
    public void processCommandSelection(EntityPlayerMP player, StructureEntityInfo structureEntityInfo, BlockPos point1, BlockPos point2, String[] args) throws CommandException
    {
        if (args.length < 3)
        {
            throw ServerTranslations.wrongUsageException("commands.selectDuplicate.usage");
        }

        int rotations = args.length >= 4 ? parseInt(args[3]) : 0;
        boolean mirrorX = args.length >= 5 && parseBoolean(args[4]);

        BlockArea area = new BlockArea(point1, point2);
        BlockPos lowerCorner = area.getLowerCorner();

        BlockPos coord = RCCommands.parseBlockPos(lowerCorner, args, 0, false);

        IvWorldData worldData = new IvWorldData(player.worldObj, area, true);
        NBTTagCompound worldDataCompound = worldData.createTagCompound(area.getLowerCorner());

        GenericStructureInfo structureInfo = GenericStructureInfo.createDefaultStructure();
        structureInfo.worldDataCompound = worldDataCompound;

        AxisAlignedTransform2D transform = AxisAlignedTransform2D.from(rotations, mirrorX);

        OperationRegistry.queueOperation(new OperationGenerateStructure(structureInfo, transform, coord, true), player);
    }
}
