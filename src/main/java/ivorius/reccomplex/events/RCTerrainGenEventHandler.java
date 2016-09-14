/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.events;

import ivorius.reccomplex.worldgen.sapling.RCSaplingGenerator;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by lukas on 14.09.16.
 */
public class RCTerrainGenEventHandler
{
    public void register()
    {
        MinecraftForge.TERRAIN_GEN_BUS.register(this);
    }

    @SubscribeEvent
    public void onSaplingGrow(SaplingGrowTreeEvent event)
    {
        if (event.getWorld() instanceof WorldServer)
        {
            if (RCSaplingGenerator.maybeGrowSapling((WorldServer) event.getWorld(), event.getPos(), event.getRand()))
            {
                event.setResult(Event.Result.DENY);
            }
        }
    }
}
