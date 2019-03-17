/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.structure;

import com.google.common.primitives.Doubles;
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
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandSearchStructure extends CommandExpecting
{
    public static final int MAX_RESULTS = 20;

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

            sender.sendMessage(ServerTranslations.join("", new TextComponentString(prefix),
                    ServerTranslations.join((Object[]) components)));
        }
        else
            sender.sendMessage(RecurrentComplex.translations.get("commands.rcsearch.empty"));
    }

    @Nonnull
    public static <T> PriorityQueue<T> search(Set<T> omega, ToDoubleFunction<T> rank)
    {
        PriorityQueue<T> strucs = new PriorityQueue<>(10, (o1, o2) -> Doubles.compare(rank.applyAsDouble(o1), rank.applyAsDouble(o2)));
        strucs.addAll(omega.stream().filter(s -> rank.applyAsDouble(s) > 0).collect(Collectors.toList()));
        return strucs;
    }

    public static void consider(List<ToDoubleFunction<String>> ranks, Parameter<String> parameter, Parameter.Function<Parameter<String>, ToDoubleFunction<Structure<?>>> supplier) throws CommandException
    {
        if (!parameter.has(1))
            return;

        ToDoubleFunction<Structure<?>> fun = supplier.apply(parameter);
        ranks.add(name -> fun.applyAsDouble(StructureRegistry.INSTANCE.get(name)));
    }

    public static <T> void consider(List<ToDoubleFunction<String>> ranks, Parameter<String> parameter, Function<Parameter<String>, Parameter<T>> fun, ToDoubleBiFunction<Structure<?>, T> rank) throws CommandException
    {
        consider(ranks, parameter, param ->
        {
            T t = param.to(fun).require();
            return structure -> rank.applyAsDouble(structure, t);
        });
    }

    @Override
    public String getName()
    {
        return "search";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void expect(Expect expect)
    {
        expect
                .skip().descriptionU("terms").optional().repeat()
                .named("containing", "c").words(MCE::block).descriptionU("block expression")
                .named("biome", "b").then(MCE::biome).descriptionU("biome id")
                .named("dimension", "d").then(MCE::dimension).descriptionU("dimension id")
                .named("maze").skip().descriptionU("maze id")
                .named("list").skip().descriptionU("structure list id")
                .named("author").skip().descriptionU("author")
                .flag("all", "a")
        ;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        Parameters parameters = Parameters.of(args, expect()::declare);

        List<ToDoubleFunction<String>> ranks = new ArrayList<>();

        consider(ranks, parameters.get(0), Parameter::varargsList,
                (s, t) -> StructureSearch.searchRank(t, StructureSearch.keywords(StructureRegistry.INSTANCE.id(s), s)));

        consider(ranks, parameters.get("containing"),
                e -> RCP.expression(e, new BlockExpression(RecurrentComplex.specialRegistry)),
                StructureSearch::containedBlocks);
        consider(ranks, parameters.get("biome"), MCP::biome, StructureSearch::biome);
        consider(ranks, parameters.get("dimension"), MCP.dimension(server, sender), StructureSearch::dimension);
        consider(ranks, parameters.get("maze"), p -> p, StructureSearch::maze);
        consider(ranks, parameters.get("list"), p -> p, StructureSearch::list);
        consider(ranks, parameters.get("author"), p -> p, StructureSearch::author);

        boolean all = parameters.has("all");

        if (ranks.stream().noneMatch(Objects::nonNull))
            throw new WrongUsageException(getUsage(sender));

        postResultMessage("Results: ", sender,
                RCTextStyle::structure,
                search(all ? StructureRegistry.INSTANCE.ids() : StructureRegistry.INSTANCE.activeIDs(),
                        name -> ranks.stream()
                                .filter(Objects::nonNull)
                                .mapToDouble(f -> f.applyAsDouble(name))
                                .reduce(1, (a, b) -> a * b)
                )
        );
    }
}
