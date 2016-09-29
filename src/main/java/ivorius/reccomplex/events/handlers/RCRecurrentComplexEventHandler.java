/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.events.handlers;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.events.RCEventBus;
import ivorius.reccomplex.events.FileLoadEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by lukas on 14.09.16.
 */
public class RCRecurrentComplexEventHandler
{
    public void register()
    {
        RCEventBus.INSTANCE.register(this);
    }

    @SubscribeEvent
    public void onLoad(FileLoadEvent.Pre event)
    {
        if (!RCConfig.generateNature && event.domain.equals(RecurrentComplex.MOD_ID) &&
                (event.path.getParent().endsWith("nature") || event.path.getParent().getParent().endsWith("nature")))
            event.shouldGenerate = false;
    }
}
