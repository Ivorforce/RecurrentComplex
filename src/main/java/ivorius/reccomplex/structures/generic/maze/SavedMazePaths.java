/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.maze;

import com.google.common.base.Function;
import com.google.gson.*;
import ivorius.ivtoolkit.maze.MazePath;
import ivorius.ivtoolkit.maze.MazeRoom;
import ivorius.ivtoolkit.tools.NBTCompoundObject;
import ivorius.reccomplex.json.JsonUtils;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

/**
 * Created by lukas on 14.04.15.
 */
public class SavedMazePaths
{
    public static Function<SavedMazePath, MazePath> toPathFunction()
    {
        return new Function<SavedMazePath, MazePath>()
        {
            @Nullable
            @Override
            public MazePath apply(SavedMazePath input)
            {
                return input != null ? input.toPath() : null;
            }
        };
    }
}

