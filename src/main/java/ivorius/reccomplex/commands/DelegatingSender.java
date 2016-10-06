/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Created by lukas on 06.10.16.
 */
public class DelegatingSender implements ICommandSender
{
    private final ICommandSender sender;

    public DelegatingSender(ICommandSender sender)
    {
        this.sender = sender;
    }

    @Override
    public String getName()
    {
        return sender.getName();
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return sender.getDisplayName();
    }

    @Override
    public void addChatMessage(ITextComponent component)
    {
        sender.addChatMessage(component);
    }

    @Override
    public boolean canCommandSenderUseCommand(int permLevel, String commandName)
    {
        return sender.canCommandSenderUseCommand(permLevel, commandName);
    }

    @Override
    public BlockPos getPosition()
    {
        return sender.getPosition();
    }

    @Override
    public Vec3d getPositionVector()
    {
        return sender.getPositionVector();
    }

    @Override
    public World getEntityWorld()
    {
        return sender.getEntityWorld();
    }

    @Nullable
    @Override
    public Entity getCommandSenderEntity()
    {
        return sender.getCommandSenderEntity();
    }

    @Override
    public boolean sendCommandFeedback()
    {
        return sender.sendCommandFeedback();
    }

    @Override
    public void setCommandStat(CommandResultStats.Type type, int amount)
    {
        sender.setCommandStat(type, amount);
    }

    @Nullable
    @Override
    public MinecraftServer getServer()
    {
        return sender.getServer();
    }
}
