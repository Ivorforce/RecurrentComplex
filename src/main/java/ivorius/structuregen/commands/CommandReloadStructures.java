/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.commands;

import ivorius.structuregen.worldgen.StructureSaveHandler;
import ivorius.structuregen.worldgen.inventory.InventoryGeneratorSaveHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandReloadStructures extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "reloadStructures";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return "commands.reloadStructures.usage";
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args)
    {
        StructureSaveHandler.reloadAllCustomStructures();
        InventoryGeneratorSaveHandler.reloadAllCustomInventoryGenerators();
    }
}
