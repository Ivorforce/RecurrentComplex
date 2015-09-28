/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.structures.generic.StructureSaveHandler;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.worldgen.inventory.ItemCollectionSaveHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

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
        return ServerTranslations.usage("commands.strucReload.usage");
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args)
    {
        RecurrentComplex.fileTypeRegistry.reloadCustomFiles();
        commandSender.addChatMessage(ServerTranslations.format("commands.strucReload.success"));
    }
}
