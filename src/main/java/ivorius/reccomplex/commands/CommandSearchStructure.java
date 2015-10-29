/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
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
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.regex.Pattern;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandSearchStructure extends CommandBase
{
    public static final int MAX_RESULTS = 20;

    public static ChatComponentText createStructureChatComponent(String strucID)
    {
        ChatComponentText comp = new ChatComponentText(strucID);
        comp.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                String.format("/%s %s", RCCommands.lookup.getCommandName(), strucID)));
        comp.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                ServerTranslations.format("commands.rcsearch.lookup")));
        comp.getChatStyle().setColor(EnumChatFormatting.BLUE);
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

        return Iterables.any(keywords, Predicates.contains(Pattern.compile(Strings.join(Lists.transform(query, new Function<String, String>()
        {
            @Nullable
            @Override
            public String apply(String s)
            {
                return Pattern.quote(s);
            }
        }), "|")))) ? 1 : 0;
    }

    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "search";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.rcsearch.usage");
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args)
    {
        if (args.length >= 1)
        {
            final List<String> query = Arrays.asList(args);

            PriorityQueue<String> strucs = new PriorityQueue<>(10, new Comparator<String>()
            {
                @Override
                public int compare(String o1, String o2)
                {
                    float r1 = searchRank(query, o1, StructureRegistry.INSTANCE.getStructure(o1));
                    float r2 = searchRank(query, o2, StructureRegistry.INSTANCE.getStructure(o2));
                    return Floats.compare(r1, r2);
                }
            });
            for (String s : StructureRegistry.INSTANCE.allStructureIDs())
            {
                if (searchRank(query, s, StructureRegistry.INSTANCE.getStructure(s)) > 0)
                    strucs.add(s);
            }

            boolean cut = strucs.size() > MAX_RESULTS;
            ChatComponentText[] components = new ChatComponentText[cut ? MAX_RESULTS : strucs.size()];
            for (int i = 0; i < components.length; i++)
            {
                if (cut && i == components.length - 1)
                    components[i] = new ChatComponentText("... (" + strucs.size() + ")");
                else
                    components[i] = createStructureChatComponent(strucs.remove());
            }

            commandSender.addChatMessage(new ChatComponentTranslation(StringUtils.repeat("%s", ", ", components.length), (Object[]) components));
        }
        else
        {
            throw ServerTranslations.commandException("commands.rclookup.usage");
        }
    }
}
