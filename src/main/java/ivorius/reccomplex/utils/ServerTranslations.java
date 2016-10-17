/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.RecurrentComplex;
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
public class ServerTranslations
{
    public static Object[] convertParams(Object... params)
    {
        if (RecurrentComplex.isLite())
        {
            Object[] array = new Object[params.length];

            for (int i = 0; i < array.length; i++)
            {
                if (params[i] instanceof TextComponentTranslation)
                    array[i] = IvTranslations.format(((TextComponentTranslation) params[i]).getKey(), convertParams(((TextComponentTranslation) params[i]).getFormatArgs()));
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

    public static String usage(String key)
    {
        if (RecurrentComplex.isLite())
            return IvTranslations.get(key);
        else
            return key;
    }

    public static ITextComponent get(String key)
    {
        if (RecurrentComplex.isLite())
            return new TextComponentString(IvTranslations.get(key));
        else
            return new TextComponentTranslation(key);
    }

    public static ITextComponent format(String key, Object... params)
    {
        if (RecurrentComplex.isLite())
            return new TextComponentString(IvTranslations.format(key, convertParams(params)));
        else
            return new TextComponentTranslation(key, params);
    }

    public static CommandException wrongUsageException(String key, Object... params)
    {
        if (RecurrentComplex.isLite())
            return new WrongUsageException(IvTranslations.format(key, convertParams(params)));
        else
            return new WrongUsageException(key, params);
    }

    public static CommandException commandException(String key, Object... params)
    {
        if (RecurrentComplex.isLite())
            return new CommandException(IvTranslations.format(key, convertParams(params)));
        else
            return new CommandException(key, params);
    }

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
}
