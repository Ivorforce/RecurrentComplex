/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;
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
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
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

    public static float searchRank(List<String> query, String id, StructureInfo structure)
    {
        return structure instanceof GenericStructureInfo ? searchRank(query, id, ((GenericStructureInfo) structure).metadata) : 0;
    }

    public static float searchRank(List<String> query, String id, Metadata metadata)
    {
        List<String> keywords = Lists.newArrayList(id);
        keywords.add(metadata.authors);
        keywords.add(metadata.comment);
        keywords.add(metadata.weblink);

        return keywords.stream().anyMatch(Predicates.contains(Pattern.compile(Strings.join(Lists.transform(query, Pattern::quote), "|")))::apply) ? 1 : 0;
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
            final List<String> query = Arrays.asList(args);

            PriorityQueue<String> strucs = new PriorityQueue<>(10, (o1, o2) -> {
                float r1 = searchRank(query, o1, StructureRegistry.INSTANCE.getStructure(o1));
                float r2 = searchRank(query, o2, StructureRegistry.INSTANCE.getStructure(o2));
                return Floats.compare(r1, r2);
            });
            strucs.addAll(StructureRegistry.INSTANCE.allStructureIDs().stream().filter(s -> searchRank(query, s, StructureRegistry.INSTANCE.getStructure(s)) > 0).collect(Collectors.toList()));

            if (strucs.size() > 0)
            {
                boolean cut = strucs.size() > MAX_RESULTS;
                TextComponentString[] components = new TextComponentString[cut ? MAX_RESULTS : strucs.size()];
                for (int i = 0; i < components.length; i++)
                {
                    if (cut && i == components.length - 1)
                        components[i] = new TextComponentString("... (" + strucs.size() + ")");
                    else
                        components[i] = createStructureTextComponent(strucs.remove());
                }

                commandSender.addChatMessage(new TextComponentTranslation(StringUtils.repeat("%s", ", ", components.length), (Object[]) components));
            }
            else
            {
                commandSender.addChatMessage(ServerTranslations.get("commands.rcsearch.empty"));
            }
        }
        else
        {
            throw ServerTranslations.commandException("commands.rclookup.usage");
        }
    }
}
