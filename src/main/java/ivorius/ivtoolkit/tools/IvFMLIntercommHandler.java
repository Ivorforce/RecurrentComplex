/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.ivtoolkit.tools;

import cpw.mods.fml.common.event.FMLInterModComms;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Logger;

/**
 * Created by lukas on 07.06.14.
 */
public abstract class IvFMLIntercommHandler
{
    private Logger logger;
    private String modOwnerID;
    private Object modInstance;

    protected IvFMLIntercommHandler(Logger logger, String modOwnerID, Object modInstance)
    {
        this.logger = logger;
        this.modOwnerID = modOwnerID;
        this.modInstance = modInstance;
    }

    public Logger getLogger()
    {
        return logger;
    }

    public void setLogger(Logger logger)
    {
        this.logger = logger;
    }

    public String getModOwnerID()
    {
        return modOwnerID;
    }

    public void setModOwnerID(String modOwnerID)
    {
        this.modOwnerID = modOwnerID;
    }

    public Object getModInstance()
    {
        return modInstance;
    }

    public void setModInstance(Object modInstance)
    {
        this.modInstance = modInstance;
    }

    public void handleMessages(boolean server, boolean runtime)
    {
        for (FMLInterModComms.IMCMessage message : FMLInterModComms.fetchRuntimeMessages(modInstance))
        {
            onIMCMessage(message, server, true);
        }
    }

    public void onIMCMessage(FMLInterModComms.IMCMessage message, boolean server, boolean runtime)
    {
        try
        {
            boolean didHandle = handleMessage(message, server, runtime);

            if (!didHandle)
            {
                logger.warn("Could not handle message with key '" + message.key + "' of type '" + message.getMessageType().getName() + "'");
            }
        }
        catch (Exception ex)
        {
            logger.error("Exception on message with key '" + message.key + "' of type '" + message.getMessageType().getName() + "'");
            ex.printStackTrace();
        }
    }

    protected abstract boolean handleMessage(FMLInterModComms.IMCMessage message, boolean server, boolean runtime);

    protected boolean isMessage(String key, FMLInterModComms.IMCMessage message, Class expectedType)
    {
        if (key.equals(message.key))
        {
            if (message.getMessageType().isAssignableFrom(expectedType))
            {
                return true;
            }

            faultyMessage(message, expectedType);
        }

        return false;
    }

    protected Entity getEntity(NBTTagCompound compound, boolean server)
    {
        return getEntity(compound, "worldID", "entityID", server);
    }

    protected Entity getEntity(NBTTagCompound compound, String worldKey, String entityKey, boolean server)
    {
        if (!server)
        {
            return Minecraft.getMinecraft().theWorld.getEntityByID(compound.getInteger(entityKey));
        }
        else
        {
            return MinecraftServer.getServer().worldServerForDimension(compound.getInteger(worldKey)).getEntityByID(compound.getInteger(entityKey));
        }
    }

    protected boolean sendReply(FMLInterModComms.IMCMessage message, String value)
    {
        if (message.getSender() == null)
        {
            return false;
        }

        NBTTagCompound cmp = message.getNBTValue();
        FMLInterModComms.sendRuntimeMessage(modOwnerID, message.getSender(), cmp.getString("replyKey"), value);
        return true;
    }

    protected boolean sendReply(FMLInterModComms.IMCMessage message, NBTTagCompound value)
    {
        if (message.getSender() == null)
        {
            return false;
        }

        NBTTagCompound cmp = message.getNBTValue();
        FMLInterModComms.sendRuntimeMessage(modOwnerID, message.getSender(), cmp.getString("replyKey"), value);
        return true;
    }

    protected boolean sendReply(FMLInterModComms.IMCMessage message, ItemStack value)
    {
        if (message.getSender() == null)
        {
            logger.error("Message error! Could not reply to message with key '" + message.key + "' - No sender found");
            return false;
        }

        NBTTagCompound cmp = message.getNBTValue();
        FMLInterModComms.sendRuntimeMessage(modOwnerID, message.getSender(), cmp.getString("replyKey"), value);
        return true;
    }

    private void faultyMessage(FMLInterModComms.IMCMessage message, Class expectedType)
    {
        logger.error("Got message with key '" + message.key + "' of type '" + message.getMessageType().getName() + "'; Expected type: '" + expectedType.getName() + "'");
    }
}

