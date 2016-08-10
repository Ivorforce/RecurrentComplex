/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.utils.BlockSurfacePos;
import net.minecraft.command.CommandException;
import net.minecraft.util.BlockPos;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.operation.OperationRegistry;
import ivorius.reccomplex.structures.OperationGenerateStructure;
import ivorius.reccomplex.structures.StructureInfos;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.StructureInfo;
import ivorius.reccomplex.structures.generic.gentypes.NaturalGenerationInfo;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.worldgen.StructureGenerator;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

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

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.strucGen.usage");
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length <= 0)
            throw ServerTranslations.wrongUsageException("commands.strucGen.usage");

        String structureName = args[0];
        StructureInfo structureInfo = StructureRegistry.INSTANCE.getStructure(structureName);
        World world = args.length >= 4 ? DimensionManager.getWorld(parseInt(args[2])) : commandSender.getEntityWorld();

        if (structureInfo == null)
        {
            throw ServerTranslations.commandException("commands.strucGen.noStructure", structureName);
        }

        BlockSurfacePos coord;

        if (args.length >= 3)
            coord = RCCommands.parseSurfaceBlockPos(commandSender, args, 1, false);
        else
            coord = BlockSurfacePos.from(commandSender.getPosition());

        if (structureInfo instanceof GenericStructureInfo)
        {
            Random random = world.rand;

            AxisAlignedTransform2D transform = AxisAlignedTransform2D.from(structureInfo.isRotatable() ? random.nextInt(4) : 0, structureInfo.isMirrorable() && random.nextBoolean());

            int[] size = StructureInfos.structureSize(structureInfo, transform);

            int genX = coord.getX() - size[0] / 2;
            int genZ = coord.getZ() - size[2] / 2;
            int genY;
            List<NaturalGenerationInfo> naturalGenerationInfos = structureInfo.generationInfos(NaturalGenerationInfo.class);
            if (naturalGenerationInfos.size() > 0)
                genY = naturalGenerationInfos.get(0).ySelector.selectY(world, random, StructureInfos.structureBoundingBox(new BlockPos(genX, 0, genZ), size));
            else
                genY = world.getHeight(coord.blockPos(0)).getY();

            BlockPos genCoord = new BlockPos(genX, genY, genZ);

            OperationRegistry.queueOperation(new OperationGenerateStructure((GenericStructureInfo) structureInfo, transform, genCoord, false, structureName), commandSender);
        }
        else
            StructureGenerator.randomInstantly(world, world.rand, structureInfo, null, coord, false, structureName);
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, StructureRegistry.INSTANCE.allStructureIDs());
        else if (args.length == 2 || args.length == 3)
        {
            return getListOfStringsMatchingLastWord(args, "~");
        }

        return null;
    }
}
