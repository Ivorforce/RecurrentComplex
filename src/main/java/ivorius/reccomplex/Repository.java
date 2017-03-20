/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by lukas on 20.03.17.
 */
public class Repository
{
    public static final String BASE_URL = "http://complex.ivorius.de";

    public static void openWebLink(String url)
    {
        try
        {
            openWebLink(new URI(url));
        }
        catch (URISyntaxException e)
        {
            RecurrentComplex.logger.error("Can\'t open url for {}", url, e);
        }
    }

    public static void openWebLink(URI uri)
    {
        try
        {
            Class<?> oclass = Class.forName("java.awt.Desktop");
            Object object = oclass.getMethod("getDesktop", new Class[0]).invoke(null);
            oclass.getMethod("browse", new Class[] {URI.class}).invoke(object, uri);
        }
        catch (Throwable throwable1)
        {
            Throwable throwable = throwable1.getCause();
            RecurrentComplex.logger.error("Couldn\'t open link: {}", throwable == null ? "<UNKNOWN>" : throwable.getMessage());
        }
    }

    public static String browseURL()
    {
        return String.format("%s/submissions",
                BASE_URL
        );
    }

    public static String submitURL(@Nullable String id)
    {
        return String.format("%s/submissions/create%s",
                BASE_URL,
                id != null ? "?title=" + id : ""
        );
    }
}
