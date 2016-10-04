/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.world.gen.feature.structure.*;
import ivorius.reccomplex.world.gen.feature.structure.generic.gentypes.StructureGenerationInfo;
import ivorius.reccomplex.utils.BlockSurfacePos;
import ivorius.reccomplex.world.gen.feature.StructureGenerator;
import net.minecraft.command.CommandException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.operation.OperationRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.gentypes.NaturalGenerationInfo;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructureInfo;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
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

        generateStructure(commandSender, args, 0, 1, commandSender, 3, 4);
    }

    public static void generateStructure(ICommandSender commandSender, String[] args, int idIndex, int posIndex, ICommandSender posRef, int dimIndex, int genInfoIndex) throws CommandException
    {
        String structureName = args[idIndex];
        StructureInfo<?> structureInfo = StructureRegistry.INSTANCE.get(structureName);
        WorldServer world = args.length > dimIndex ? DimensionManager.getWorld(parseInt(args[dimIndex])) : (WorldServer) commandSender.getEntityWorld();

        if (structureInfo == null)
            throw ServerTranslations.commandException("commands.strucGen.noStructure", structureName);

        BlockSurfacePos pos;

        pos = RCCommands.tryParseSurfaceBlockPos(posRef, args, posIndex, false);

        StructureGenerationInfo generationInfo;

        if (args.length > genInfoIndex)
            generationInfo = structureInfo.generationInfo(args[genInfoIndex]);
        else
            generationInfo = structureInfo.<StructureGenerationInfo>generationInfos(NaturalGenerationInfo.class).stream()
                    .findFirst().orElse(structureInfo.generationInfos(StructureGenerationInfo.class).stream().findFirst().orElse(null));

        Placer placer = generationInfo.placer();

        if (structureInfo instanceof GenericStructureInfo)
        {
            GenericStructureInfo genericStructureInfo = (GenericStructureInfo) structureInfo;

            StructureGenerator<GenericStructureInfo.InstanceData> generator = new StructureGenerator<>(genericStructureInfo).world(world)
                    .randomPosition(pos, placer).fromCenter(true);

            Optional<BlockPos> lowerCoord = generator.lowerCoord();
            if (lowerCoord.isPresent())
                OperationRegistry.queueOperation(new OperationGenerateStructure(genericStructureInfo, generationInfo.id(), generator.transform(), lowerCoord.get(), false, structureName), commandSender);
            else
                throw ServerTranslations.commandException("commands.strucGen.noPlace");
        }
        else
        {
            if (!new StructureGenerator<>(structureInfo).world(world).generationInfo(generationInfo)
                    .structureID(structureName).randomPosition(pos, placer).fromCenter(true).generate().isPresent())
                throw ServerTranslations.commandException("commands.strucGen.noPlace");
        }
    }

    @Nonnull
    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 2 || args.length == 3)
            return getTabCompletionCoordinateXZ(args, args.length - 1, pos);

        return tabCompletionOptions(args, 0, 3, 4);
    }

    @Nonnull
    public static List<String> tabCompletionOptions(String[] args, int idIndex, int dimIndex, int genInfoIndex)
    {
        if (args.length == idIndex + 1)
        {
            return getListOfStringsMatchingLastWord(args, StructureRegistry.INSTANCE.ids());
        }
        else if (args.length == dimIndex + 1)
            return getListOfStringsMatchingLastWord(args, Arrays.stream(DimensionManager.getIDs()).map(String::valueOf).collect(Collectors.toList()));
        else if (args.length == genInfoIndex + 1)
        {
            String structureName = args[idIndex];
            StructureInfo<?> structureInfo = StructureRegistry.INSTANCE.get(structureName);
            if (structureInfo instanceof GenericStructureInfo)
                return getListOfStringsMatchingLastWord(args, structureInfo.generationInfos(StructureGenerationInfo.class).stream().map(StructureGenerationInfo::id).collect(Collectors.toList()));
        }

        return Collections.emptyList();
    }
}
