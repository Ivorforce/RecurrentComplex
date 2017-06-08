/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.mcopts;

import ivorius.reccomplex.mcopts.translation.ServerTranslations;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by lukas on 08.06.17.
 */
public class MCOpts
{
    public static final String NAME = "MCOpts";
    //    public static final String MOD_ID = "mcopts";
    public static final String VERSION = "1.0";

    public static Logger logger = LogManager.getLogger(NAME);

    public static final ServerTranslations translations = new ServerTranslations()
    {
        @Override
        public boolean translateServerSide()
        {
            return true;
        }
    };
}
