/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.blocks;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.utils.RCAccessorCommandBase;
import net.minecraft.command.CommandBase;
import net.minecraft.command.IAdminCommand;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

/**
 * Created by lukas on 11.02.15.
 */
public class SpawnCommandLogic implements ICommandSender
{
    private TileEntitySpawnCommand tileEntity;
    private String command = "";

    public SpawnCommandLogic(TileEntitySpawnCommand tileEntity, String command)
    {
        this.tileEntity = tileEntity;
        this.command = command;
    }

    @Override
    public boolean canCommandSenderUseCommand(int p_70003_1_, String p_70003_2_)
    {
        return p_70003_1_ <= 2;
    }

    @Override
    public ChunkCoordinates getPlayerCoordinates()
    {
        return new ChunkCoordinates(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
    }

    @Override
    public World getEntityWorld()
    {
        return tileEntity.getWorldObj();
    }

    public void setCommand(String p_145752_1_)
    {
        this.command = p_145752_1_;
    }

    public String getCommand()
    {
        return this.command;
    }

    public void executeCommand(World world)
    {
        MinecraftServer minecraftserver = MinecraftServer.getServer();

        if (minecraftserver != null)
        {
            IAdminCommand cachedAdmin = null;
            if (!RCConfig.notifyAdminOnBlockCommands)
            {
                cachedAdmin = RCAccessorCommandBase.getCommandAdmin();
                CommandBase.setAdminCommander(null);
            }

            ICommandManager icommandmanager = minecraftserver.getCommandManager();
            icommandmanager.executeCommand(this, command);

            if (!RCConfig.notifyAdminOnBlockCommands)
                CommandBase.setAdminCommander(cachedAdmin);
        }
    }

    @Override
    public String getCommandSenderName()
    {
        return "@";
    }

    @Override
    public IChatComponent func_145748_c_()
    {
        return new ChatComponentText(this.getCommandSenderName());
    }

    @Override
    public void addChatMessage(IChatComponent message)
    {

    }
}