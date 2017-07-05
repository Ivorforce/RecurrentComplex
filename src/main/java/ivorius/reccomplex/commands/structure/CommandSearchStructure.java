/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.structure;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.mcopts.commands.CommandExpecting;
import ivorius.mcopts.commands.parameters.MCP;
import ivorius.mcopts.commands.parameters.Parameter;
import ivorius.mcopts.commands.parameters.Parameters;
import ivorius.mcopts.commands.parameters.expect.Expect;
import ivorius.mcopts.commands.parameters.expect.MCE;
import ivorius.mcopts.translation.ServerTranslations;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.commands.RCTextStyle;
import ivorius.reccomplex.commands.parameters.RCP;
import ivorius.reccomplex.utils.expression.BlockExpression;
import ivorius.reccomplex.world.gen.feature.selector.StructureSelector;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import ivorius.reccomplex.world.gen.feature.structure.generic.Metadata;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.GenerationType;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.NaturalGeneration;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.VanillaDecorationGeneration;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandSearchStructure extends CommandExpecting
{
    public static final int MAX_RESULTS = 20;

    @Nonnull
    public static Collection<String> keywords(String id, Structure<?> structure)
    {
        List<String> keywords = new ArrayList<>();

        keywords.add(id);

        structure.generationTypes(GenerationType.class).forEach(info -> keywords(keywords, info));

        if (structure instanceof GenericStructure)
            keywords(keywords, ((GenericStructure) structure).metadata);

        return keywords;
    }

    protected static void keywords(Collection<String> keywords, GenerationType info)
    {
        keywords.add(info.id());
        keywords.add(info.displayString());
        keywords.add(StructureRegistry.GENERATION_TYPES.iDForType(info.getClass()));
    }

    @Nonnull
    public static void keywords(Collection<String> collection, Metadata metadata)
    {
        collection.add(metadata.authors);
        collection.add(metadata.comment);
        collection.add(metadata.weblink);
    }

    public static float searchRank(List<String> query, Collection<String> keywords)
    {
        return keywords.stream().filter(Predicates.contains(Pattern.compile(String.join("|", Lists.transform(query, Pattern::quote)), Pattern.CASE_INSENSITIVE))::apply).count();
    }

    public static <T> void postResultMessage(String prefix, ICommandSender sender, Function<T, ? extends ITextComponent> toComponent, Queue<T> list)
    {
        if (list.size() > 0)
        {
            boolean cut = list.size() > MAX_RESULTS;

            ITextComponent[] components = new TextComponentBase[cut ? MAX_RESULTS : list.size()];
            for (int i = 0; i < components.length; i++)
            {
                if (cut && i == components.length - 1)
                    components[i] = new TextComponentString("... (" + list.size() + ")");
                else
                    components[i] = toComponent.apply(list.remove());
            }

            sender.addChatMessage(ServerTranslations.join("", new TextComponentString(prefix),
                    ServerTranslations.join((Object[]) components)));
        }
        else
            sender.addChatMessage(RecurrentComplex.translations.get("commands.rcsearch.empty"));
    }

    @Nonnull
    public static <T> PriorityQueue<T> search(Set<T> omega, ToDoubleFunction<T> rank)
    {
        PriorityQueue<T> strucs = new PriorityQueue<>(10, (o1, o2) -> Doubles.compare(rank.applyAsDouble(o1), rank.applyAsDouble(o2)));
        strucs.addAll(omega.stream().filter(s -> rank.applyAsDouble(s) > 0).collect(Collectors.toList()));
        return strucs;
    }

    public static long containedBlocks(Structure structure, BlockExpression matcher)
    {
        if (structure == null)
            return 0;

        IvBlockCollection collection = structure.blockCollection();

        if (collection == null)
            return 0;

        return collection.area().stream()
                .anyMatch(p -> matcher.evaluate(collection.getBlockState(p))) ? 1 : 0;
    }

    public static ToDoubleFunction<String> searchRank(Parameter<String> parameter) throws CommandException
    {
        if (!parameter.has(1))
            return null;

        List<String> terms = parameter.varargsList().optional().orElse(null);
        return name -> searchRank(terms, keywords(name, StructureRegistry.INSTANCE.get(name)));
    }

    public static ToDoubleFunction<String> containedRank(Parameter<String> parameter) throws CommandException
    {
        if (!parameter.has(1))
            return null;

        BlockExpression matcher = parameter.to(RCP.expression(new BlockExpression(RecurrentComplex.specialRegistry))).require();
        return name -> CommandSearchStructure.containedBlocks(StructureRegistry.INSTANCE.get(name), matcher);
    }

    public static ToDoubleFunction<String> biomeRank(Parameter<String> parameter) throws CommandException
    {
        if (!parameter.has(1))
            return null;

        Biome biome = parameter.to(MCP::biome).require();
        return name ->
        {
            Structure<?> structure = StructureRegistry.INSTANCE.get(name);

            double result = 0;

            result += structure.generationTypes(NaturalGeneration.class).stream()
                    .mapToDouble(g -> StructureSelector.generationWeightInBiome(g.biomeWeights, biome))
                    .sum();

            result += structure.generationTypes(VanillaDecorationGeneration.class).stream()
                    .mapToDouble(g -> StructureSelector.generationWeightInBiome(g.biomeWeights, biome))
                    .sum();

            return result;
        };
    }

    public static ToDoubleFunction<String> dimensionRank(Parameter<String> parameter, MinecraftServer server) throws CommandException
    {
        if (!parameter.has(1))
            return null;

        WorldServer world = parameter.to(MCP.dimension(server, server)).require();
        return name ->
        {
            Structure<?> structure = StructureRegistry.INSTANCE.get(name);

            double result = 0;

            result += structure.generationTypes(NaturalGeneration.class).stream()
                    .mapToDouble(g -> StructureSelector.generationWeightInDimension(g.dimensionWeights, world.provider))
                    .sum();

            result += structure.generationTypes(VanillaDecorationGeneration.class).stream()
                    .mapToDouble(g -> StructureSelector.generationWeightInDimension(g.dimensionWeights, world.provider))
                    .sum();

            return result;
        };
    }

    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "search";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void expect(Expect expect)
    {
        expect
                .skip().descriptionU("terms").required().repeat()
                .named("containing", "c").words(MCE::block).descriptionU("block expression")
                .named("biome", "b").then(MCE::biome).descriptionU("biome id")
                .named("dimension", "d").then(MCE::dimension).descriptionU("dimension id")
        ;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        Parameters parameters = Parameters.of(args, expect()::declare);

        List<ToDoubleFunction<String>> ranks = new ArrayList<>();

        ranks.add(searchRank(parameters.get(0)));
        ranks.add(containedRank(parameters.get("containing")));
        ranks.add(biomeRank(parameters.get("biome")));
        ranks.add(dimensionRank(parameters.get("dimension"), server));

        if (ranks.stream().noneMatch(Objects::nonNull))
            throw new WrongUsageException(getCommandUsage(sender));

        postResultMessage("Results: ", sender,
                RCTextStyle::structure,
                search(StructureRegistry.INSTANCE.ids(),
                        name -> ranks.stream()
                                .filter(Objects::nonNull)
                                .mapToDouble(f -> f.applyAsDouble(name))
                                .reduce(1, (a, b) -> a * b)
                )
        );
    }
}
