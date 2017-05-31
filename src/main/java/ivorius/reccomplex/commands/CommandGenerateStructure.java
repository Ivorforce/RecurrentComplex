/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockSurfacePos;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.commands.parameters.Expect;
import ivorius.reccomplex.commands.parameters.Parameters;
import ivorius.reccomplex.operation.OperationRegistry;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.StructureGenerator;
import ivorius.reccomplex.world.gen.feature.structure.OperationGenerateStructure;
import ivorius.reccomplex.world.gen.feature.structure.Placer;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.GenerationType;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
        Parameters parameters = Parameters.of(this, args);

        String structureID = parameters.get().here().require();
        Structure<?> structure = parameters.get().structure().require();
        WorldServer world = parameters.get("d").dimension(commandSender).require();
        AxisAlignedTransform2D transform = RCCommands.transform(parameters.get("r"), parameters.get("m")).optional().orElse(null);
        GenerationType generationType = parameters.get("g").generationType(structure).require();
        BlockSurfacePos pos = parameters.get("p").surfacePos(commandSender, false).require();

        Placer placer = generationType.placer();

        StructureGenerator<?> generator = new StructureGenerator<>(structure).world(world).generationInfo(generationType)
                .structureID(structureID).randomPosition(pos, placer).fromCenter(true)
                .transform(transform);

        if (structure instanceof GenericStructure)
        {
            GenericStructure genericStructureInfo = (GenericStructure) structure;

            Optional<BlockPos> lowerCoord = generator.lowerCoord();
            if (lowerCoord.isPresent())
                OperationRegistry.queueOperation(new OperationGenerateStructure(genericStructureInfo, generationType.id(), generator.transform(), lowerCoord.get(), false).withStructureID(structureID).prepare(world), commandSender);
            else
                throw ServerTranslations.commandException("commands.strucGen.noPlace");
        }
        else
        {
            if (generator.generate() == null)
                throw ServerTranslations.commandException("commands.strucGen.noPlace");
        }
    }

    @Nonnull
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        Parameters parameters = Parameters.of(this, args);

        return Expect.start()
                .next(StructureRegistry.INSTANCE.ids())
                .named("p").surfacePos(pos)
                .named("d")
                .next(RCCommands::completeDimension)
                .named("g")
                .next((String[] args1) ->
                {
                    Structure<?> structure = parameters.get().structure().optional().orElse(null);
                    if (structure instanceof GenericStructure)
                        return getListOfStringsMatchingLastWord(args1, structure.generationTypes(GenerationType.class).stream().map(GenerationType::id).collect(Collectors.toList()));
                    return Collections.emptyList();
                })
                .named("r")
                .next(RCCommands::completeRotation)
                .named("m")
                .next(RCCommands::completeMirror)
                .get(server, sender, args, pos);

        //        else if (args.length == 6)
//            return getListOfStringsMatchingLastWord(args, "0", "2", "5");
//        else if (args.length == 7)
//            return getListOfStringsMatchingLastWord(args, "fade", "up", "down", "fog");
    }

}
