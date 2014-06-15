package ivorius.structuregen;

import cpw.mods.fml.common.event.FMLInterModComms;
import ivorius.structuregen.ivtoolkit.IvFMLIntercommHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Logger;

/**
 * Created by lukas on 07.06.14.
 */
public class SGCommunicationHandler extends IvFMLIntercommHandler
{
    public SGCommunicationHandler(Logger logger, String modOwnerID, Object modInstance)
    {
        super(logger, modOwnerID, modInstance);
    }

    @Override
    protected boolean handleMessage(FMLInterModComms.IMCMessage message, boolean server, boolean runtime)
    {
        if (isMessage("addStructure", message, String.class))
        {

        }

        return false;
    }
}
