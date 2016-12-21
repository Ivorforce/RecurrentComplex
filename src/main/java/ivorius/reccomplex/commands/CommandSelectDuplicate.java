/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.operation.OperationRegistry;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.structure.OperationGenerateStructure;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructureInfo;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectDuplicate extends CommandBase
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

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length < 3)
        {
            throw ServerTranslations.wrongUsageException("commands.selectDuplicate.usage");
        }

        int rotations = args.length >= 4 ? parseInt(args[3]) : 0;
        boolean mirrorX = args.length >= 5 && parseBoolean(args[4]);

        SelectionOwner selectionOwner = RCCommands.getSelectionOwner(commandSender, null, true);
        BlockArea area = selectionOwner.getSelection();
        BlockPos lowerCorner = area.getLowerCorner();

        BlockPos coord = RCCommands.parseBlockPos(lowerCorner, args, 0, false);

        IvWorldData worldData = IvWorldData.capture(commandSender.getEntityWorld(), area, true);
        NBTTagCompound worldDataCompound = worldData.createTagCompound(area.getLowerCorner());

        GenericStructureInfo structureInfo = GenericStructureInfo.createDefaultStructure();
        structureInfo.worldDataCompound = worldDataCompound;

        AxisAlignedTransform2D transform = AxisAlignedTransform2D.from(rotations, mirrorX);

        OperationRegistry.queueOperation(new OperationGenerateStructure(structureInfo, null, transform, coord, true).prepare((WorldServer) commandSender.getEntityWorld()), commandSender);
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length <= 3)
            return getTabCompletionCoordinate(args, args.length - 1, pos);
        else if (args.length == 4 || args.length == 5)
            return RCCommands.completeTransform(args, args.length - 4);

        return super.getTabCompletionOptions(server, sender, args, pos);
    }
}
