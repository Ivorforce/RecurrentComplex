/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.events.handlers;

import ivorius.reccomplex.worldgen.decoration.RCBiomeDecorator;
import ivorius.reccomplex.worldgen.sapling.RCSaplingGenerator;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
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

    @SubscribeEvent
    public void onDecoration(DecorateBiomeEvent.Decorate event)
    {
        if (event.getWorld() instanceof WorldServer)
        {
            RCBiomeDecorator.DecorationType type;
            switch (event.getType())
            {
                case BIG_SHROOM:
                    type = RCBiomeDecorator.DecorationType.BIG_SHROOM;
                    break;
                case TREE:
                    type = RCBiomeDecorator.DecorationType.TREE;
                    break;
                case CACTUS:
                    type = RCBiomeDecorator.DecorationType.CACTUS;
                    break;
                case FOSSIL:
                    type = RCBiomeDecorator.DecorationType.FOSSIL;
                    break;
                case DESERT_WELL:
                    type = RCBiomeDecorator.DecorationType.DESERT_WELL;
                    break;
                default:
                    type = null;
            }

            if (type != null)
            {
                if (RCBiomeDecorator.decorate((WorldServer) event.getWorld(), event.getRand(), event.getPos(), type))
                    event.setResult(Event.Result.DENY);
            }
        }
    }
}
