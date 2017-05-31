/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.commands.parameters.RCExpect;
import ivorius.reccomplex.commands.parameters.RCParameters;
import ivorius.reccomplex.utils.RCStrings;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import ivorius.reccomplex.world.gen.feature.structure.generic.Metadata;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandLookupStructure extends CommandBase
{
    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "lookup";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.rclookup.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args);

        String id = parameters.get().first().require();
        GenericStructure structure = parameters.rc().genericStructure().require();

        Metadata metadata = structure.metadata;

        boolean hasWeblink = !metadata.weblink.trim().isEmpty();
        ITextComponent weblink = hasWeblink ? new TextComponentString(RCStrings.abbreviateFormatted(metadata.weblink, 30)) : ServerTranslations.format("commands.rclookup.reply.nolink");
        if (hasWeblink)
        {
            weblink.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, metadata.weblink));
            weblink.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(metadata.weblink)));
        }

        ITextComponent level = new TextComponentString(StructureRegistry.INSTANCE.status(id).getLevel().toString());
        level.getStyle().setColor(TextFormatting.YELLOW);

        commandSender.sendMessage(ServerTranslations.format(
                StructureRegistry.INSTANCE.hasActive(id) ? "commands.rclookup.reply.generates" : "commands.rclookup.reply.silent",
                id, RCTextStyle.users(metadata.authors), level, weblink));

        if (!metadata.comment.trim().isEmpty())
            commandSender.sendMessage(ServerTranslations.format("commands.rclookup.reply.comment", metadata.comment));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        return RCExpect.startRC()
                .structure()
                .get(server, sender, args, pos);
    }
}
