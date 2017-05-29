/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.utils.expression.DimensionMatcher;
import ivorius.reccomplex.world.gen.feature.selector.StructureSelector;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.GenerationType;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.NaturalGeneration;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.VanillaDecorationGeneration;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.VanillaGeneration;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;

import java.util.List;
import java.util.PriorityQueue;
import java.util.function.BiPredicate;
import java.util.stream.IntStream;
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

        addStructureLog(commandSender, (s, structure) ->
                !structure.generationTypes(GenerationType.class).isEmpty(), "Missing generation type");

        addGenerationLog(commandSender, NaturalGeneration.class, (structure, gen) ->
                        Biome.REGISTRY.getKeys().stream().anyMatch(b -> StructureSelector.generationWeightInBiome(gen.biomeWeights, Biome.REGISTRY.getObject(b)) > 0)
                , "Natural generation type won't accept any biome");

        addGenerationLog(commandSender, NaturalGeneration.class, (structure, gen) ->
                        Stream.of(DimensionManager.getIDs()).anyMatch(d -> StructureSelector.generationWeightInDimension(gen.dimensionWeights, server.worldServerForDimension(d).provider) > 0)
                , "Natural generation type won't accept any dimensions");

        addGenerationLog(commandSender, NaturalGeneration.class, (structure, gen) ->
                        gen.getActiveGenerationWeight() > 0
                , "Natural generation type has no weight");


        addGenerationLog(commandSender, VanillaGeneration.class, (structure, gen) ->
                        Biome.REGISTRY.getKeys().stream().anyMatch(b -> gen.biomeMatcher.test(Biome.REGISTRY.getObject(b)))
                , "Vanilla structure generation type won't accept any biome");

        addGenerationLog(commandSender, VanillaGeneration.class, (structure, gen) ->
                gen.getActiveWeight() > 0
                , "Vanilla structure generation type has no weight");


        addGenerationLog(commandSender, VanillaDecorationGeneration.class, (structure, gen) ->
                        Biome.REGISTRY.getKeys().stream().anyMatch(b -> StructureSelector.generationWeightInBiome(gen.biomeWeights, Biome.REGISTRY.getObject(b)) > 0)
                , "Vanilla structure generation type won't accept any biome");

        addGenerationLog(commandSender, VanillaDecorationGeneration.class, (structure, gen) ->
                        Stream.of(DimensionManager.getIDs()).anyMatch(d -> StructureSelector.generationWeightInDimension(gen.dimensionWeights, server.worldServerForDimension(d).provider) > 0)
                , "Natural generation type won't accept any dimensions");
    }

    protected <T extends GenerationType> void addGenerationLog(ICommandSender commandSender, Class<T> tClass, BiPredicate<Structure<?>, T> predicate, String msg)
    {
        addStructureLog(commandSender, (s, structure) -> structure.generationTypes(tClass).stream().allMatch(gen -> predicate.test(structure, gen)), msg);
    }

    protected void addStructureLog(ICommandSender commandSender, BiPredicate<String, Structure<?>> predicate, String msg)
    {
        PriorityQueue<String> structures = CommandSearchStructure.search(StructureRegistry.INSTANCE.activeIDs(), (String name) ->
                predicate.test(name, StructureRegistry.INSTANCE.get(name)) ? 0 : 1);
        if (structures.size() > 0)
        {
            commandSender.addChatMessage(new TextComponentString(msg + ":"));
            CommandSearchStructure.postResultMessage(commandSender, RCTextStyle::structure, structures);
        }
    }

}
