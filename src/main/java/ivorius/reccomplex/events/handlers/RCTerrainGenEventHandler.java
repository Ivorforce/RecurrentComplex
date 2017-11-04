/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.events.handlers;

import ivorius.reccomplex.world.gen.feature.decoration.RCBiomeDecorator;
import ivorius.reccomplex.world.gen.feature.sapling.RCSaplingGenerator;
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
    private static boolean hasAmountData(DecorateBiomeEvent.Decorate event)
    {
        try
        {
            // Ensure
            event.getClass().getDeclaredMethod("getModifiedAmount");
            event.getClass().getDeclaredMethod("setModifiedAmount", int.class);
            return (boolean) event.getClass().getDeclaredMethod("hasAmountData").invoke(event);
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private static int getModifiedAmount(DecorateBiomeEvent.Decorate event)
    {
        try
        {
            return (int) event.getClass().getDeclaredMethod("getModifiedAmount").invoke(event);
        }
        catch (Exception ignored)
        {
            return -1;
        }
    }

    private static void setModifiedAmount(DecorateBiomeEvent.Decorate event, int amount)
    {
        try
        {
            event.getClass().getDeclaredMethod("setModifiedAmount", int.class).invoke(event, amount);
        }
        catch (Exception ignored)
        {
        }
    }

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
            RCBiomeDecorator.DecorationType type = RCBiomeDecorator.DecorationType.getDecorationType(event);

            if (type != null)
            {
                int amount;
                if (hasAmountData(event) && (amount = getModifiedAmount(event)) >= 0)
                    setModifiedAmount(event, RCBiomeDecorator.decorate((WorldServer) event.getWorld(), event.getRand(), event.getPos(), type, amount));
                else
                {
                    Event.Result result = RCBiomeDecorator.decorate((WorldServer) event.getWorld(), event.getRand(), event.getPos(), type);
                    if (result != null)
                        event.setResult(result);
                }
            }
        }
    }

}
