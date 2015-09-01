/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.operation.OperationRegistry;
import ivorius.reccomplex.structures.OperationGenerateStructure;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.StructureInfo;
import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.worldgen.StructureGenerator;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandImportStructure extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "import";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.strucImport.usage");
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args)
    {
        int x, y, z;

        if (args.length <= 0)
        {
            throw ServerTranslations.wrongUsageException("commands.strucImport.usage");
        }

        String structureName = args[0];
        StructureInfo structureInfo = StructureRegistry.getStructure(structureName);
        World world = commandSender.getEntityWorld();

        if (structureInfo == null)
        {
            throw ServerTranslations.commandException("commands.strucImport.noStructure", structureName);
        }

        x = commandSender.getPlayerCoordinates().posX;
        y = commandSender.getPlayerCoordinates().posY;
        z = commandSender.getPlayerCoordinates().posZ;

        if (args.length >= 4)
        {
            x = MathHelper.floor_double(func_110666_a(commandSender, (double) x, args[1]));
            y = MathHelper.floor_double(func_110666_a(commandSender, (double) y, args[2]));
            z = MathHelper.floor_double(func_110666_a(commandSender, (double) z, args[3]));
        }

        AxisAlignedTransform2D transform = AxisAlignedTransform2D.ORIGINAL;
        BlockCoord coord = new BlockCoord(x, y, z);

        if (structureInfo instanceof GenericStructureInfo)
            OperationRegistry.queueOperation(new OperationGenerateStructure((GenericStructureInfo) structureInfo, transform, coord, true), commandSender);
        else
            StructureGenerator.directly(structureInfo, StructureSpawnContext.complete(world, world.rand, transform, coord, structureInfo, 0, true));
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args)
    {
        if (args.length == 1)
            return getListOfStringsFromIterableMatchingLastWord(args, StructureRegistry.allStructureIDs());
        else if (args.length == 2 || args.length == 3 || args.length == 4)
        {
            return getListOfStringsMatchingLastWord(args, "~");
        }

        return null;
    }
}
