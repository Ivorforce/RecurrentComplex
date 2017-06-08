/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.mcopts.translation;

import net.minecraft.command.CommandException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Created by lukas on 01.04.15.
 */
public abstract class ServerTranslations
{

    @Nonnull
    public static TextComponentTranslation join(Object... components)
    {
        return join(", ", components);
    }

    @Nonnull
    public static TextComponentTranslation join(String join, Object... components)
    {
        return new TextComponentTranslation(StringUtils.repeat("%s", join, components.length), components);
    }

    @Nonnull
    public static TextComponentTranslation join(List<?> components)
    {
        return join(components.toArray());
    }

    public CommandException commandException(String key, Object... params)
    {
        if (translateServerSide())
            return new CommandException(Translations.format(key, convertParams(params)));
        else
            return new CommandException(key, params);
    }

    public CommandException wrongUsageException(String key, Object... params)
    {
        if (translateServerSide())
            return new WrongUsageException(Translations.format(key, convertParams(params)));
        else
            return new WrongUsageException(key, params);
    }

    public ITextComponent format(String key, Object... params)
    {
        if (translateServerSide())
            return new TextComponentString(Translations.format(key, convertParams(params)));
        else
            return new TextComponentTranslation(key, params);
    }

    public ITextComponent get(String key)
    {
        if (translateServerSide())
            return new TextComponentString(Translations.get(key));
        else
            return new TextComponentTranslation(key);
    }

    public String usage(String key)
    {
        if (translateServerSide())
            return Translations.get(key);
        else
            return key;
    }

    public Object[] convertParams(Object... params)
    {
        if (translateServerSide())
        {
            Object[] array = new Object[params.length];

            for (int i = 0; i < array.length; i++)
            {
                if (params[i] instanceof TextComponentTranslation)
                    array[i] = Translations.format(((TextComponentTranslation) params[i]).getKey(), this.convertParams(((TextComponentTranslation) params[i]).getFormatArgs()));
                else if (params[i] instanceof ITextComponent)
                    array[i] = ((ITextComponent) params[i]).getUnformattedText();
                else
                    array[i] = params[i];
            }

            return array;
        }
        else
            return params;
    }

    public abstract boolean translateServerSide();
}
