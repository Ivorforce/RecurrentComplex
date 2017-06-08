/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters.expect;

import ivorius.reccomplex.mcopts.commands.parameters.expect.Expect;
import ivorius.reccomplex.mcopts.commands.parameters.expect.MCE;
import ivorius.reccomplex.dimensions.DimensionDictionary;

import java.util.function.Consumer;

/**
 * Created by lukas on 31.05.17.
 */
public class IvE
{
    public static Consumer<Expect> surfacePos(String x, String z)
    {
        return e -> e.named(x).then(MCE::x)
                .named(z).then(MCE::z)
                .atOnce(2);
    }

    public static void dimensionType(Expect e)
    {
        e.next(DimensionDictionary.allRegisteredTypes()).descriptionU("dimension type");
    }
}
