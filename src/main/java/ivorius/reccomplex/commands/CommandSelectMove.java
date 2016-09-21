/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import net.minecraft.command.CommandException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.operation.OperationRegistry;
import ivorius.reccomplex.structures.OperationMoveStructure;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectMove extends CommandSelectModify
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "move";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.selectMove.usage");
    }

    @Override
    public void executeSelection(EntityPlayerMP player, StructureEntityInfo structureEntityInfo, BlockPos point1, BlockPos point2, String[] args) throws CommandException
    {
        if (args.length < 3)
        {
            throw ServerTranslations.wrongUsageException("commands.selectMove.usage");
        }

        int rotations = args.length >= 4 ? parseInt(args[3]) : 0;
        boolean mirrorX = args.length >= 5 && parseBoolean(args[4]);

        BlockArea area = new BlockArea(point1, point2);

        BlockPos coord = RCCommands.parseBlockPos(area.getLowerCorner(), args, 0, false);

        IvWorldData worldData = IvWorldData.capture(player.worldObj, area, true);
        NBTTagCompound worldDataCompound = worldData.createTagCompound(area.getLowerCorner());

        GenericStructureInfo structureInfo = GenericStructureInfo.createDefaultStructure();
        structureInfo.worldDataCompound = worldDataCompound;

        AxisAlignedTransform2D transform = AxisAlignedTransform2D.from(rotations, mirrorX);

        OperationRegistry.queueOperation(new OperationMoveStructure(structureInfo, transform, coord, true, area), player);
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length <= 3)
            return getTabCompletionCoordinate(args, args.length - 1, pos);
        else if (args.length == 4)
            return getListOfStringsMatchingLastWord(args, "0", "1", "2", "3");
        else if (args.length == 5)
            return getListOfStringsMatchingLastWord(args, "true", "false");


        return super.getTabCompletionOptions(server, sender, args, pos);
    }
}
