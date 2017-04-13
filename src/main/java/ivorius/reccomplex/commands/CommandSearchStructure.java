/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import ivorius.reccomplex.world.gen.feature.structure.generic.Metadata;
import ivorius.reccomplex.world.gen.feature.structure.generic.gentypes.GenerationType;
import joptsimple.internal.Strings;
import net.minecraft.command.CommandBase;
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
public class CommandSearchStructure extends CommandBase
{
    public static final int MAX_RESULTS = 20;

    @Nonnull
    public static Collection<String> keywords(String id, Structure<?> structure)
    {
        List<String> keywords = new ArrayList<>();

        keywords.add(id);

        structure.generationInfos(GenerationType.class).forEach(info -> keywords(keywords, info));

        if (structure instanceof GenericStructure)
            keywords(keywords, ((GenericStructure) structure).metadata);

        return keywords;
    }

    protected static void keywords(Collection<String> keywords, GenerationType info)
    {
        keywords.add(info.id());
        keywords.add(info.displayString());
        keywords.add(StructureRegistry.GENERATION_INFOS.iDForType(info.getClass()));
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
        return keywords.stream().filter(Predicates.contains(Pattern.compile(Strings.join(Lists.transform(query, Pattern::quote), "|"), Pattern.CASE_INSENSITIVE))::apply).count();
    }

    public static <T> void outputSearch(ICommandSender commandSender, Set<T> omega, ToDoubleFunction<T> rank, Function<T, ? extends ITextComponent> toComponent)
    {
        PriorityQueue<T> results = search(omega, rank);

        if (results.size() > 0)
        {
            boolean cut = results.size() > MAX_RESULTS;
            ITextComponent[] components = new TextComponentBase[cut ? MAX_RESULTS : results.size()];
            for (int i = 0; i < components.length; i++)
            {
                if (cut && i == components.length - 1)
                    components[i] = new TextComponentString("... (" + results.size() + ")");
                else
                    components[i] = toComponent.apply(results.remove());
            }

            commandSender.sendMessage(ServerTranslations.join((Object[]) components));
        }
        else
        {
            commandSender.sendMessage(ServerTranslations.get("commands.rcsearch.empty"));
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
    public String getUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.rcsearch.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length >= 1)
        {
            outputSearch(commandSender, StructureRegistry.INSTANCE.ids(),
                    name -> searchRank(Arrays.asList(args), keywords(name, StructureRegistry.INSTANCE.get(name))),
                    RCTextStyle::structure
            );
        }
        else
            throw ServerTranslations.commandException("commands.rcsearch.usage");
    }
}
