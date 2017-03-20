/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.Repository;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 20.03.17.
 */
public class RCTextStyle
{
    @Nonnull
    protected static ITextComponent path(String path, String id)
    {
        ITextComponent pathComponent = new TextComponentString(String.format("%s/%s", path, id));
        pathComponent.getStyle().setColor(TextFormatting.GOLD);
        return pathComponent;
    }

    @Nonnull
    protected static ITextComponent submit(String id)
    {
        ITextComponent submit = ServerTranslations.get("reccomplex.save.submit");
        submit.getStyle().setColor(TextFormatting.AQUA);
        submit.getStyle().setBold(true);
        submit.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ServerTranslations.get("reccomplex.save.submit.hover")));
        submit.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Repository.submitURL(id)));
        return submit;
    }

    public static ITextComponent structure(String id)
    {
        TextComponentString comp = new TextComponentString(id);
        comp.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                String.format("/%s %s", RCCommands.lookup.getName(), id)));
        comp.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                ServerTranslations.get("commands.rcsearch.lookup")));
        comp.getStyle().setColor(TextFormatting.AQUA);
        return comp;
    }
}
