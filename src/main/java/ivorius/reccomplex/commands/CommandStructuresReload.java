/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.structures.generic.StructureSaveHandler;
import ivorius.reccomplex.worldgen.inventory.CustomGenericItemCollectionHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentTranslation;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandStructuresReload extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "reload";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return "commands.strucReload.usage";
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args)
    {
        StructureSaveHandler.reloadAllCustomStructures();
        CustomGenericItemCollectionHandler.reloadAllCustomInventoryGenerators();
        commandSender.addChatMessage(new ChatComponentTranslation("commands.strucReload.success"));
    }
}
