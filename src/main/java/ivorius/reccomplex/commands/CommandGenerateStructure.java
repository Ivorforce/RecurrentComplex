/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.world.gen.feature.structure.*;
import ivorius.reccomplex.world.gen.feature.structure.generic.gentypes.GenerationType;
import ivorius.ivtoolkit.blocks.BlockSurfacePos;
import ivorius.reccomplex.world.gen.feature.StructureGenerator;
import net.minecraft.command.CommandException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.operation.OperationRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.gentypes.NaturalGeneration;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.world.WorldServer;

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
    public String getName()
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
    public String getUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.strucGen.usage");
    }

    @Override
    @ParametersAreNonnullByDefault
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length <= 0)
            throw ServerTranslations.wrongUsageException("commands.strucGen.usage");

        String structureName = args[0];
        Structure<?> structure = StructureRegistry.INSTANCE.get(structureName);
        WorldServer world = RCCommands.tryParseDimension(commandSender, args, 3);

        if (structure == null)
            throw ServerTranslations.commandException("commands.strucGen.noStructure", structureName);

        BlockSurfacePos pos;

        pos = RCCommands.tryParseSurfaceBlockPos(commandSender, args, 1, false);

        GenerationType generationType;

        if (args.length > 4)
            generationType = structure.generationInfo(args[4]);
        else
            generationType = structure.<GenerationType>generationInfos(NaturalGeneration.class).stream()
                    .findFirst().orElse(structure.generationInfos(GenerationType.class).stream().findFirst().orElse(null));

        Placer placer = generationType.placer();

        if (structure instanceof GenericStructure)
        {
            GenericStructure genericStructureInfo = (GenericStructure) structure;

            StructureGenerator<GenericStructure.InstanceData> generator = new StructureGenerator<>(genericStructureInfo).world(world)
                    .randomPosition(pos, placer).fromCenter(true);

            Optional<BlockPos> lowerCoord = generator.lowerCoord();
            if (lowerCoord.isPresent())
                OperationRegistry.queueOperation(new OperationGenerateStructure(genericStructureInfo, generationType.id(), generator.transform(), lowerCoord.get(), false).withStructureID(structureName).prepare(world), commandSender);
            else
                throw ServerTranslations.commandException("commands.strucGen.noPlace");
        }
        else
        {
            if (!new StructureGenerator<>(structure).world(world).generationInfo(generationType)
                    .structureID(structureName).randomPosition(pos, placer).fromCenter(true).generate().isPresent())
                throw ServerTranslations.commandException("commands.strucGen.noPlace");
        }
    }

    @Nonnull
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 2 || args.length == 3)
            return getTabCompletionCoordinateXZ(args, 0, pos);

        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, StructureRegistry.INSTANCE.ids());
        else if (args.length == 4)
            return RCCommands.completeDimension(args);
        else if (args.length == 5)
        {
            String structureName = args[0];
            Structure<?> structure = StructureRegistry.INSTANCE.get(structureName);
            if (structure instanceof GenericStructure)
                return getListOfStringsMatchingLastWord(args, structure.generationInfos(GenerationType.class).stream().map(GenerationType::id).collect(Collectors.toList()));
        }
//        else if (args.length == 6)
//            return getListOfStringsMatchingLastWord(args, "0", "2", "5");
//        else if (args.length == 7)
//            return getListOfStringsMatchingLastWord(args, "fade", "up", "down", "fog");

        return Collections.emptyList();
    }

}
