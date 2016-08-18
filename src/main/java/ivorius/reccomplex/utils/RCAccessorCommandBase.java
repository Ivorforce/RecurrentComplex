/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandListener;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

/**
 * Created by lukas on 18.01.15.
 */
public class RCAccessorCommandBase
{
    private static Field commandAdmin;

    private static void initializeUniqueID()
    {
        if (commandAdmin == null)
            commandAdmin = ReflectionHelper.findField(CommandBase.class, "field_71533_a", "commandListener");
    }

    public static ICommandListener getCommandAdmin()
    {
        initializeUniqueID();

        try
        {
            return (ICommandListener) commandAdmin.get(null);
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
