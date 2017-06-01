/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockSurfacePos;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.commands.parameters.RCExpect;
import ivorius.reccomplex.commands.parameters.RCParameters;
import ivorius.reccomplex.operation.OperationRegistry;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.StructureGenerator;
import ivorius.reccomplex.world.gen.feature.structure.OperationGenerateStructure;
import ivorius.reccomplex.world.gen.feature.structure.Placer;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
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
        RCParameters parameters = RCParameters.of(args, "mirror");

        String structureID = parameters.get().first().require();
        Structure<?> structure = parameters.rc().structure().require();
        WorldServer world = parameters.mc("dimension").dimension(server, commandSender).require();
        AxisAlignedTransform2D transform = parameters.transform("rotation", "mirror").optional().orElse(null);
        GenerationType generationType = parameters.rc("gen").generationType(structure).require();
        BlockSurfacePos pos = parameters.surfacePos("x", "z", commandSender.getPosition(), false).require();
        String seed = parameters.get("seed").first().optional().orElse(null);

        Placer placer = generationType.placer();

        StructureGenerator<?> generator = new StructureGenerator<>(structure).world(world).generationInfo(generationType)
                .random(seed != null ? new Random(seed.hashCode()) : null)
                .structureID(structureID).randomPosition(pos, placer).fromCenter(true)
                .transform(transform);

        if (structure instanceof GenericStructure && world == commandSender.getEntityWorld())
        {
            GenericStructure genericStructureInfo = (GenericStructure) structure;

            Optional<BlockPos> lowerCoord = generator.lowerCoord();
            if (lowerCoord.isPresent())
                OperationRegistry.queueOperation(new OperationGenerateStructure(genericStructureInfo, generationType.id(), generator.transform(), lowerCoord.get(), false)
                        .withSeed(seed)
                        .withStructureID(structureID).prepare(world), commandSender);
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
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        RCParameters parameters = RCParameters.of(args);

        return RCExpect.startRC()
                .structure()
                .surfacePos("x", "z")
                .named("dimension").dimension()
                .named("gen")
                .next((String[] args1) -> parameters.rc().genericStructure().tryGet()
                        .map(structure -> structure.generationTypes(GenerationType.class).stream().map(GenerationType::id).collect(Collectors.toList()))
                        .orElse(Collections.emptyList()))
                .named("rotation").rotation()
                .named("seed").randomString()
                .flag("mirror")
                .get(server, sender, args, pos);

        //        else if (args.length == 6)
//            return getListOfStringsMatchingLastWord(args, "0", "2", "5");
//        else if (args.length == 7)
//            return getListOfStringsMatchingLastWord(args, "fade", "up", "down", "fog");
    }

}
