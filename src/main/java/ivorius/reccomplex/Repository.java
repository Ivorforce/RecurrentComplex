/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex;

import javax.annotation.Nullable;

/**
 * Created by lukas on 20.03.17.
 */
public class Repository
{
    public static final String BASE_URL = "http://complex.ivorius.de";

    public static String submitURL(@Nullable String id)
    {
        return String.format("%s/submissions/create%s",
                BASE_URL,
                id != null ? "?title=" + id : ""
        );
    }
}
