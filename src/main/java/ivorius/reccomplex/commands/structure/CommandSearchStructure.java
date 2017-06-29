/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.structure;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.commands.RCTextStyle;
import ivorius.mcopts.commands.CommandExpecting;
import ivorius.mcopts.commands.parameters.*;
import ivorius.mcopts.commands.parameters.expect.Expect;
import ivorius.mcopts.translation.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import ivorius.reccomplex.world.gen.feature.structure.generic.Metadata;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.GenerationType;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextComponentString;

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

    public static <T> void postResultMessage(ICommandSender commandSender, Function<T, ? extends ITextComponent> toComponent, Queue<T> list)
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

            commandSender.sendMessage(ServerTranslations.join((Object[]) components));
        }
        else
        {
            commandSender.sendMessage(RecurrentComplex.translations.get("commands.rcsearch.empty"));
        }
    }

    @Nonnull
    public static <T> PriorityQueue<T> search(Set<T> omega, ToDoubleFunction<T> rank)
    {
        PriorityQueue<T> strucs = new PriorityQueue<>(10, (o1, o2) -> Doubles.compare(rank.applyAsDouble(o1), rank.applyAsDouble(o2)));
        strucs.addAll(omega.stream().filter(s -> rank.applyAsDouble(s) > 0).collect(Collectors.toList()));
        return strucs;
    }

    @Override
    public String getName()
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
                .skip().descriptionU("terms").required().repeat();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        Parameters parameters = Parameters.of(args, expect()::declare);

        if (args.length >= 1)
        {
            List<String> terms = parameters.get(0).varargsList().require();

            postResultMessage(commandSender,
                    RCTextStyle::structure,
                    search(StructureRegistry.INSTANCE.ids(),
                            name -> searchRank(terms, keywords(name, StructureRegistry.INSTANCE.get(name)))
                    )
            );
        }
        else
            throw RecurrentComplex.translations.commandException("commands.rcsearch.usage");
    }
}
