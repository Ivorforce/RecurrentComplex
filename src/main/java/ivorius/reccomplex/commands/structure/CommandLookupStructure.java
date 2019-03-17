/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.structure;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.commands.RCTextStyle;
import ivorius.mcopts.commands.CommandExpecting;
import ivorius.mcopts.commands.parameters.*;
import ivorius.mcopts.commands.parameters.expect.Expect;
import ivorius.reccomplex.commands.parameters.expect.RCE;
import ivorius.reccomplex.commands.parameters.RCP;
import ivorius.reccomplex.utils.RCStrings;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import ivorius.reccomplex.world.gen.feature.structure.generic.Metadata;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandLookupStructure extends CommandExpecting
{
    @Override
    public String getName()
    {
        return "lookup";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void expect(Expect expect)
    {
        expect.then(RCE::structure);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        Parameters parameters = Parameters.of(args, expect()::declare);

        String id = parameters.get(0).require();
        GenericStructure structure = parameters.get(0).to(p -> RCP.genericStructure(p, false)).require();

        Metadata metadata = structure.metadata;

        boolean hasWeblink = !metadata.weblink.trim().isEmpty();
        ITextComponent weblink = hasWeblink ? new TextComponentString(RCStrings.abbreviateFormatted(metadata.weblink, 30)) : RecurrentComplex.translations.format("commands.rclookup.reply.nolink");
        if (hasWeblink)
        {
            weblink.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, metadata.weblink));
            weblink.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(metadata.weblink)));
        }

        ITextComponent level = new TextComponentString(StructureRegistry.INSTANCE.status(id).getLevel().toString());
        level.getStyle().setColor(TextFormatting.YELLOW);

        commandSender.sendMessage(RecurrentComplex.translations.format(
                StructureRegistry.INSTANCE.hasActive(id) ? "commands.rclookup.reply.generates" : "commands.rclookup.reply.silent",
                id, RCTextStyle.users(metadata.authors), level, weblink));

        if (!metadata.comment.trim().isEmpty())
            commandSender.sendMessage(RecurrentComplex.translations.format("commands.rclookup.reply.comment", metadata.comment));
    }
}
