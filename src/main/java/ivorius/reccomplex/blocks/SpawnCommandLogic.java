/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.blocks;

import net.minecraft.command.*;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.utils.RCAccessorCommandBase;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

/**
 * Created by lukas on 11.02.15.
 */
public class SpawnCommandLogic implements ICommandSender
{
    private World world;
    BlockPos coord;
    private String command = "";

    public SpawnCommandLogic(World world, BlockPos coord, String command)
    {
        this.world = world;
        this.coord = coord;
        this.command = command;
    }

    @Override
    public boolean canCommandSenderUseCommand(int p_70003_1_, String p_70003_2_)
    {
        return p_70003_1_ <= 2;
    }

    @Override
    public BlockPos getPosition()
    {
        return coord;
    }

    @Override
    public Vec3 getPositionVector()
    {
        return new Vec3((double)coord.getX() + 0.5D, (double)coord.getY() + 0.5D, (double)coord.getZ() + 0.5D);
    }

    @Override
    public World getEntityWorld()
    {
        return world;

    }

    @Override
    public Entity getCommandSenderEntity()
    {
        return null;
    }

    @Override
    public boolean sendCommandFeedback()
    {
        return false;
    }

    @Override
    public void setCommandStat(CommandResultStats.Type type, int amount)
    {

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
    public String getName()
    {
        return "@";
    }

    @Override
    public IChatComponent getDisplayName()
    {
        return new ChatComponentText(this.getName());
    }

    @Override
    public void addChatMessage(IChatComponent message)
    {

    }
}