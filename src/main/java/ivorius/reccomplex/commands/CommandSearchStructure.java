/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.structures.StructureInfo;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import ivorius.reccomplex.structures.generic.Metadata;
import ivorius.reccomplex.utils.ServerTranslations;
import joptsimple.internal.Strings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import org.apache.commons.lang3.StringUtils;

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

    public static TextComponentString createStructureTextComponent(String strucID)
    {
        TextComponentString comp = new TextComponentString(strucID);
        comp.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                String.format("/%s %s", RCCommands.lookup.getCommandName(), strucID)));
        comp.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                ServerTranslations.get("commands.rcsearch.lookup")));
        comp.getStyle().setColor(TextFormatting.BLUE);
        return comp;
    }

    @Nonnull
    public static Collection<String> keywords(String id, StructureInfo structure)
    {
        return structure instanceof GenericStructureInfo
                ? keywords(id, ((GenericStructureInfo) structure).metadata)
                : Collections.singleton(id);
    }

    @Nonnull
    public static Collection<String> keywords(String id, Metadata metadata)
    {
        List<String> keywords = Lists.newArrayList(id);
        keywords.add(metadata.authors);
        keywords.add(metadata.comment);
        keywords.add(metadata.weblink);
        return keywords;
    }

    public static float searchRank(List<String> query, Collection<String> keywords)
    {
        return keywords.stream().filter(Predicates.contains(Pattern.compile(Strings.join(Lists.transform(query, Pattern::quote), "|"), Pattern.CASE_INSENSITIVE))::apply).count();
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
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.rcsearch.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length >= 1)
        {
            outputSearch(commandSender, StructureRegistry.INSTANCE.ids(),
                    name ->
                    {
                        return searchRank(Arrays.asList(args), keywords(name, StructureRegistry.INSTANCE.get(name)));
                    },
                    CommandSearchStructure::createStructureTextComponent
            );
        }
        else
            throw ServerTranslations.commandException("commands.rclookup.usage");
    }

    public static <T> void outputSearch(ICommandSender commandSender, Set<T> omega, ToDoubleFunction<T> rank, Function<T, TextComponentBase> toComponent)
    {
        PriorityQueue<T> results = search(omega, rank);

        if (results.size() > 0)
        {
            boolean cut = results.size() > MAX_RESULTS;
            TextComponentBase[] components = new TextComponentBase[cut ? MAX_RESULTS : results.size()];
            for (int i = 0; i < components.length; i++)
            {
                if (cut && i == components.length - 1)
                    components[i] = new TextComponentString("... (" + results.size() + ")");
                else
                    components[i] = toComponent.apply(results.remove());
            }

            commandSender.addChatMessage(new TextComponentTranslation(StringUtils.repeat("%s", ", ", components.length), (Object[]) components));
        }
        else
        {
            commandSender.addChatMessage(ServerTranslations.get("commands.rcsearch.empty"));
        }
    }

    @Nonnull
    public static <T> PriorityQueue<T> search(Set<T> omega, ToDoubleFunction<T> rank)
    {
        PriorityQueue<T> strucs = new PriorityQueue<>(10, (o1, o2) -> Doubles.compare(rank.applyAsDouble(o1), rank.applyAsDouble(o2)));
        strucs.addAll(omega.stream().filter(s -> rank.applyAsDouble(s) > 0).collect(Collectors.toList()));
        return strucs;
    }
}
