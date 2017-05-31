/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.block;

import io.netty.buffer.ByteBuf;
import net.minecraft.command.*;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.utils.accessor.RCAccessorCommandBase;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.text.SimpleDateFormat;

/**
 * Created by lukas on 11.02.15.
 */
public abstract class SpawnCommandLogic implements ICommandSender
{
    /** The formatting for the timestamp on commands run. */
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("HH:mm:ss");
    /** The command stored in the command block. */
    private String commandStored = "";
    /** The custom name of the command block. (defaults to "@") */
    private String customName = "@";
    private final CommandResultStats resultStats = new CommandResultStats();

    public NBTTagCompound writeToNBT(NBTTagCompound p_189510_1_)
    {
        p_189510_1_.setString("Command", this.commandStored);
        p_189510_1_.setString("CustomName", this.customName);

        this.resultStats.writeStatsToNBT(p_189510_1_);
        return p_189510_1_;
    }

    public void readDataFromNBT(NBTTagCompound nbt)
    {
        this.commandStored = nbt.getString("Command");

        if (nbt.hasKey("CustomName", 8))
        {
            this.customName = nbt.getString("CustomName");
        }

        this.resultStats.readStatsFromNBT(nbt);
    }

    @Override
    public boolean canCommandSenderUseCommand(int permLevel, String commandName)
    {
        return permLevel <= 2;
    }

    public void setCommand(String command)
    {
        this.commandStored = command;
    }

    public String getCommand()
    {
        return this.commandStored;
    }

    public void trigger(World worldIn)
    {
        if (!worldIn.isRemote)
        {
            MinecraftServer minecraftserver = this.getServer();

            if (minecraftserver != null && minecraftserver.isAnvilFileSet() && minecraftserver.isCommandBlockEnabled())
            {
                ICommandManager icommandmanager = minecraftserver.getCommandManager();

                ICommandListener cachedAdmin = null;
                if (!RCConfig.notifyAdminOnBlockCommands)
                {
                    cachedAdmin = RCAccessorCommandBase.getCommandAdmin();
                    CommandBase.setCommandListener(null);
                }

                try
                {
                    icommandmanager.executeCommand(this, this.commandStored);
                }
                catch (Exception ex)
                {
                    CrashReport crashreport = CrashReport.makeCrashReport(ex, "Executing command block");
                    CrashReportCategory crashreportcategory = crashreport.makeCategory("Command to be executed");
                    crashreportcategory.setDetail("Command", this::getCommand);
                    crashreportcategory.setDetail("Name", this::getName);
                    throw new ReportedException(crashreport);
                }

                if (!RCConfig.notifyAdminOnBlockCommands)
                    CommandBase.setCommandListener(cachedAdmin);
            }
        }
    }

    @Override
    public String getName()
    {
        return this.customName;
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return new TextComponentString(this.getName());
    }

    public void setName(String name)
    {
        this.customName = name;
    }

    @Override
    public void addChatMessage(ITextComponent component)
    {
    }

    @Override
    public boolean sendCommandFeedback()
    {
        MinecraftServer minecraftserver = this.getServer();
        return minecraftserver == null || !minecraftserver.isAnvilFileSet() || minecraftserver.worldServers[0].getGameRules().getBoolean("commandBlockOutput");
    }

    @Override
    public void setCommandStat(CommandResultStats.Type type, int amount)
    {
        this.resultStats.setCommandStatForSender(this.getServer(), this, type, amount);
    }

    public abstract void updateCommand();

    @SideOnly(Side.CLIENT)
    public abstract int getCommandBlockType();

    @SideOnly(Side.CLIENT)
    public abstract void fillInInfo(ByteBuf buf);

    public CommandResultStats getCommandResultStats()
    {
        return this.resultStats;
    }
}