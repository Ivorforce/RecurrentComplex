/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.world.MockWorld;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.ICommandSender;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectNatural extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "natural";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.selectNatural.usage");
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, "0", "1", "2");

        return super.getTabCompletionOptions(server, sender, args, pos);
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        World world = commandSender.getEntityWorld();

        SelectionOwner selectionOwner = RCCommands.getSelectionOwner(commandSender, null, true);
        RCCommands.assertSize(commandSender, selectionOwner);
        BlockArea area = selectionOwner.getSelection();

        double expandFloor = args.length >= 1 ? parseDouble(args[0]) : 1;

        CommandSelectFloor.placeNaturalFloor(new MockWorld.Real(world), area, expandFloor);
        CommandSelectSpace.placeNaturalAir(world, area, 3, 3);
    }
}
