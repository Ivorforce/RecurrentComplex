/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.events.handlers;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.events.RCEventBus;
import ivorius.reccomplex.events.StructureRegistrationEvent;
import ivorius.reccomplex.worldgen.sapling.RCSaplingGenerator;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
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
    public void onRegistration(StructureRegistrationEvent.Pre event)
    {
        if (event.domain.equals(RecurrentComplex.MOD_ID) && event.path.getParent().endsWith("trees") && !RCConfig.generateTrees)
            event.shouldGenerate = false;
    }
}
