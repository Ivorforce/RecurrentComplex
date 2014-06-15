package ivorius.structuregen.events;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import ivorius.structuregen.StructureGen;
import ivorius.structuregen.entities.StructureEntityInfo;

/**
 * Created by lukas on 24.05.14.
 */
public class SGFMLEventHandler
{
    public void register()
    {
        FMLCommonHandler.instance().bus().register(this);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        StructureEntityInfo structureEntityInfo = StructureEntityInfo.getStructureEntityInfo(event.player);
        if (structureEntityInfo != null)
        {
            structureEntityInfo.update(event.player);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent event)
    {
        if ((event.type == TickEvent.Type.CLIENT || event.type == TickEvent.Type.SERVER) && event.phase == TickEvent.Phase.END)
        {
            StructureGen.communicationHandler.handleMessages(event.type == TickEvent.Type.SERVER, true);
        }
    }
}