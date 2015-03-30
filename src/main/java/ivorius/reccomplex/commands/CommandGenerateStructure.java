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
import ivorius.reccomplex.structures.StructureInfos;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.StructureInfo;
import ivorius.reccomplex.structures.generic.gentypes.NaturalGenerationInfo;
import ivorius.reccomplex.worldgen.StructureGenerator;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandGenerateStructure extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "gen";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return "commands.strucGen.usage";
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args)
    {
        int x, z;

        if (args.length <= 0)
            throw new WrongUsageException("commands.strucGen.usage");

        String structureName = args[0];
        StructureInfo structureInfo = StructureRegistry.getStructure(structureName);
        World world = commandSender.getEntityWorld();

        if (structureInfo == null)
        {
            throw new CommandException("commands.strucGen.noStructure", structureName);
        }

        x = commandSender.getPlayerCoordinates().posX;
        z = commandSender.getPlayerCoordinates().posZ;

        if (args.length >= 3)
        {
            x = MathHelper.floor_double(func_110666_a(commandSender, (double) x, args[1]));
            z = MathHelper.floor_double(func_110666_a(commandSender, (double) z, args[2]));
        }

        if (structureInfo instanceof GenericStructureInfo)
        {
            Random random = world.rand;

            AxisAlignedTransform2D transform = AxisAlignedTransform2D.transform(structureInfo.isRotatable() ? random.nextInt(4) : 0, structureInfo.isMirrorable() && random.nextBoolean());

            int[] size = StructureInfos.structureSize(structureInfo, transform);

            int genX = x - size[0] / 2;
            int genZ = z - size[2] / 2;
            int genY;
            List<NaturalGenerationInfo> naturalGenerationInfos = structureInfo.generationInfos(NaturalGenerationInfo.class);
            if (naturalGenerationInfos.size() > 0)
                genY = naturalGenerationInfos.get(0).ySelector.generationY(world, random, StructureInfos.structureBoundingBox(new BlockCoord(genX, 0, genZ), size));
            else
                genY = world.getHeightValue(x, z);

            BlockCoord coord = new BlockCoord(genX, genY, genZ);

            OperationRegistry.queueOperation(new OperationGenerateStructure((GenericStructureInfo) structureInfo, transform, coord, false, structureName), commandSender);
        }
        else
            StructureGenerator.randomInstantly(world, world.rand, structureInfo, null, x, z, false, structureName);
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args)
    {
        if (args.length == 1)
            return getListOfStringsFromIterableMatchingLastWord(args, StructureRegistry.getAllStructureNames());
        else if (args.length == 2 || args.length == 3)
        {
            return getListOfStringsMatchingLastWord(args, "~");
        }

        return null;
    }
}
