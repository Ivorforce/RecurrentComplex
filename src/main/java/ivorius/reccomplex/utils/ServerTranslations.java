/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import ivorius.reccomplex.RecurrentComplex;
import net.minecraft.command.CommandException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

import java.util.Arrays;

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
                if (params[i] instanceof ChatComponentTranslation)
                    array[i] = IvTranslations.format(((ChatComponentTranslation) params[i]).getKey(), convertParams(((ChatComponentTranslation) params[i]).getFormatArgs()));
                else if (params[i] instanceof IChatComponent)
                    array[i] = ((IChatComponent) params[i]).getUnformattedText();
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

    public static IChatComponent get(String key)
    {
        if (RecurrentComplex.isLite())
            return new ChatComponentText(IvTranslations.get(key));
        else
            return new ChatComponentTranslation(key);
    }

    public static IChatComponent format(String key, Object... params)
    {
        if (RecurrentComplex.isLite())
            return new ChatComponentText(IvTranslations.format(key, convertParams(params)));
        else
            return new ChatComponentTranslation(key, params);
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
}
