/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by lukas on 06.10.16.
 */
public class DelegatingSender implements ICommandSender, ICapabilityProvider
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
    public void sendMessage(ITextComponent component)
    {
        sender.sendMessage(component);
    }

    @Override
    public boolean canUseCommand(int permLevel, String commandName)
    {
        return sender.canUseCommand(permLevel, commandName);
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

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
    {
        return sender instanceof ICapabilityProvider
                && ((ICapabilityProvider) sender).hasCapability(capability, facing);

    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
    {
        return sender instanceof ICapabilityProvider
                ? ((ICapabilityProvider) sender).getCapability(capability, facing)
                : null;
    }
}
