/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.structures.*;
import ivorius.reccomplex.structures.generic.gentypes.StructureGenerationInfo;
import ivorius.reccomplex.utils.BlockSurfacePos;
import ivorius.reccomplex.worldgen.StructureGenerator;
import net.minecraft.command.CommandException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.operation.OperationRegistry;
import ivorius.reccomplex.structures.generic.gentypes.NaturalGenerationInfo;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandGenerateStructure extends CommandBase
{
    @Nonnull
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "gen";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.strucGen.usage");
    }

    @Override
    @ParametersAreNonnullByDefault
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length <= 0)
            throw ServerTranslations.wrongUsageException("commands.strucGen.usage");

        BlockSurfacePos pos;

        if (args.length >= 3)
            pos = RCCommands.parseSurfaceBlockPos(commandSender, args, 1, false);
        else
            pos = BlockSurfacePos.from(commandSender.getPosition());

        generateStructure(commandSender, args, pos, 0, 3, 4);
    }

    public static void generateStructure(ICommandSender commandSender, String[] args, BlockSurfacePos pos, int idIndex, int dimIndex, int genInfoIndex) throws CommandException
    {
        String structureName = args[idIndex];
        StructureInfo<?> structureInfo = StructureRegistry.INSTANCE.getStructure(structureName);
        WorldServer world = args.length > dimIndex ? DimensionManager.getWorld(parseInt(args[dimIndex])) : (WorldServer) commandSender.getEntityWorld();

        if (structureInfo == null)
            throw ServerTranslations.commandException("commands.strucGen.noStructure", structureName);

        StructureGenerationInfo generationInfo;

        if (args.length >= 5)
            generationInfo = structureInfo.generationInfo(args[genInfoIndex]);
        else
            generationInfo = structureInfo.<StructureGenerationInfo>generationInfos(NaturalGenerationInfo.class).stream()
                    .findFirst().orElse(structureInfo.generationInfos(StructureGenerationInfo.class).stream().findFirst().orElse(null));

        YSelector ySelector = generationInfo.ySelector();

        if (structureInfo instanceof GenericStructureInfo)
        {
            Random random = world.rand;

            AxisAlignedTransform2D transform = AxisAlignedTransform2D.from(structureInfo.isRotatable() ? random.nextInt(4) : 0, structureInfo.isMirrorable() && random.nextBoolean());
            int[] size = StructureInfos.structureSize(structureInfo, transform);

            int genX = pos.getX() - size[0] / 2;
            int genZ = pos.getZ() - size[2] / 2;
            int genY = ySelector != null
                    ? ySelector.selectY(world, random, StructureInfos.structureBoundingBox(new BlockPos(genX, 0, genZ), size))
                    : world.getHeight(pos.blockPos(0)).getY();

            BlockPos genCoord = new BlockPos(genX, genY, genZ);

            OperationRegistry.queueOperation(new OperationGenerateStructure((GenericStructureInfo) structureInfo, generationInfo.id(), transform, genCoord, false, structureName), commandSender);
        }
        else
            ((StructureGenerator) new StructureGenerator<>(structureInfo).world(world).generationInfo(generationInfo)
                                    .structureID(structureName).randomPosition(pos, ySelector).fromCenter(true)).generate();
    }

    @Nonnull
    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 2 || args.length == 3)
            return getListOfStringsMatchingLastWord(args, "~");

        return tabCompletionOptions(args, 0, 3, 4);
    }

    @Nonnull
    public static List<String> tabCompletionOptions(String[] args, int idIndex, int dimIndex, int genInfoIndex)
    {
        if (args.length == idIndex + 1)
            return getListOfStringsMatchingLastWord(args, StructureRegistry.INSTANCE.allStructureIDs());
        else if (args.length == dimIndex + 1)
            return getListOfStringsMatchingLastWord(args, Arrays.stream(DimensionManager.getIDs()).map(String::valueOf).collect(Collectors.toList()));
        else if (args.length == genInfoIndex + 1)
        {
            String structureName = args[idIndex];
            StructureInfo<?> structureInfo = StructureRegistry.INSTANCE.getStructure(structureName);
            if (structureInfo instanceof GenericStructureInfo)
                return getListOfStringsMatchingLastWord(args, structureInfo.generationInfos(StructureGenerationInfo.class).stream().map(StructureGenerationInfo::id).collect(Collectors.toList()));
        }

        return Collections.emptyList();
    }
}
