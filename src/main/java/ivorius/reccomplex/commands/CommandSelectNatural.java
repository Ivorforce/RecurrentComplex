/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.util.BlockPos;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectNatural extends CommandSelectModify
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
    public void processCommandSelection(EntityPlayerMP player, StructureEntityInfo structureEntityInfo, BlockPos point1, BlockPos point2, String[] args) throws NumberInvalidException
    {
        World world = player.getEntityWorld();

        BlockArea area = new BlockArea(point1, point2);
        double expandFloor = args.length >= 1 ? parseDouble(args[0]) : 1;

        CommandSelectFloor.placeNaturalFloor(world, area, expandFloor);
        CommandSelectSpace.placeNaturalAir(world, area, 3, 3);
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, "0", "1", "2");

        return super.addTabCompletionOptions(commandSender, args, pos);
    }
}
