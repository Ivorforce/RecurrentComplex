/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.selector.StructureSelector;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.*;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.ConnectorStrategy;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;

import java.util.PriorityQueue;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandSanity extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "sanity";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.rcsanity.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        if (StructureRegistry.INSTANCE.ids().isEmpty())
        {
            commandSender.addChatMessage(new TextComponentString("No registered structures!"));
            return;
        }

        boolean sane = true;

        sane &= addStructureLog(commandSender, (s, structure) ->
                !structure.generationTypes(GenerationType.class).isEmpty(), "Missing generation type");

        sane &= addGenerationLog(commandSender, NaturalGeneration.class, (structure, gen) ->
                        values(Biome.REGISTRY).anyMatch(b -> StructureSelector.generationWeightInBiome(gen.biomeWeights, b) > 0)
                , "Natural generation type won't accept any known biomes");

        sane &= addGenerationLog(commandSender, NaturalGeneration.class, (structure, gen) ->
                        dimensions(server).anyMatch(d -> StructureSelector.generationWeightInDimension(gen.dimensionWeights, d.provider) > 0)
                , "Natural generation type won't accept any known dimensions");

        sane &= addGenerationLog(commandSender, NaturalGeneration.class, (structure, gen) ->
                        gen.getActiveGenerationWeight() > 0
                , "Natural generation type has no weight");


        sane &= addGenerationLog(commandSender, VanillaGeneration.class, (structure, gen) ->
                        values(Biome.REGISTRY).anyMatch(b -> gen.biomeMatcher.test(b))
                , "Vanilla structure generation type won't accept any known biomes");

        sane &= addGenerationLog(commandSender, VanillaGeneration.class, (structure, gen) ->
                        gen.getActiveWeight() > 0
                , "Vanilla structure generation type has no weight");

        sane &= addGenerationLog(commandSender, VanillaGeneration.class, (structure, gen) ->
                        gen.minBaseLimit > 0 || gen.maxBaseLimit > 0 || gen.maxScaledLimit > 0 || gen.minScaledLimit > 0
                , "Vanilla structure is always limited to zero instances");


        sane &= addGenerationLog(commandSender, VanillaDecorationGeneration.class, (structure, gen) ->
                        values(Biome.REGISTRY).anyMatch(b -> StructureSelector.generationWeightInBiome(gen.biomeWeights, b) > 0)
                , "Vanilla structure generation type won't accept any known biomes");

        sane &= addGenerationLog(commandSender, VanillaDecorationGeneration.class, (structure, gen) ->
                        dimensions(server).anyMatch(d -> StructureSelector.generationWeightInDimension(gen.dimensionWeights, d.provider) > 0)
                , "Natural generation type won't accept any dimensions");


        sane &= addGenerationLog(commandSender, MazeGeneration.class, (structure, gen) ->
                        gen.getWeight() > 0
                , "Maze generation type has no weight");

        sane &= addGenerationLog(commandSender, MazeGeneration.class, (structure, gen) ->
                        !gen.getMazeID().trim().isEmpty()
                , "Maze generation type has maze id");

        sane &= addGenerationLog(commandSender, MazeGeneration.class, (structure, gen) ->
                        !gen.mazeComponent.rooms.isEmpty()
                , "Maze generation type has no rooms");

        sane &= addGenerationLog(commandSender, MazeGeneration.class, (structure, gen) ->
                        !gen.mazeComponent.exitPaths.isEmpty() || !gen.mazeComponent.defaultConnector.id.equals(ConnectorStrategy.DEFAULT_WALL)
                , "Maze generation type has no walkable exits");


        sane &= addGenerationLog(commandSender, ListGeneration.class, (structure, gen) ->
                        !gen.listID.trim().isEmpty()
                , "List generation has no list id");

        sane &= addGenerationLog(commandSender, ListGeneration.class, (structure, gen) ->
                        gen.getWeight() > 0
                , "List generation has no weight");


        sane &= addGenerationLog(commandSender, SaplingGeneration.class, (structure, gen) ->
                        gen.getActiveWeight() > 0
                , "Sapling generation has no weight");


        sane &= addGenerationLog(commandSender, StaticGeneration.class, (structure, gen) ->
                        dimensions(server).anyMatch(d -> gen.dimensionMatcher.test(d.provider))
                , "Static generation won't accept any known dimensions");

        if (sane)
            commandSender.addChatMessage(new TextComponentString("No specific problems found!"));
    }

    public static <K, T> Stream<T> values(IRegistry<K, T> registry)
    {
        return registry.getKeys().stream().map(registry::getObject);
    }

    public static Stream<World> dimensions(MinecraftServer server)
    {
        return Stream.of(DimensionManager.getIDs()).map(server::worldServerForDimension);
    }

    protected <T extends GenerationType> boolean addGenerationLog(ICommandSender commandSender, Class<T> tClass, BiPredicate<Structure<?>, T> predicate, String msg)
    {
        return addStructureLog(commandSender, (s, structure) -> structure.generationTypes(tClass).stream().allMatch(gen -> predicate.test(structure, gen)), msg);
    }

    protected boolean addStructureLog(ICommandSender commandSender, BiPredicate<String, Structure<?>> predicate, String msg)
    {
        PriorityQueue<String> structures = CommandSearchStructure.search(StructureRegistry.INSTANCE.activeIDs(), (String name) ->
                predicate.test(name, StructureRegistry.INSTANCE.get(name)) ? 0 : 1);

        if (structures.size() > 0)
        {
            commandSender.addChatMessage(new TextComponentString(msg + ":"));
            CommandSearchStructure.postResultMessage(commandSender, RCTextStyle::structure, structures);
            return false;
        }

        return true;
    }

}
