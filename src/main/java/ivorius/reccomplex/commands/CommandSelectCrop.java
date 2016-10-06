/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockAreas;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.utils.expression.PositionedBlockMatcher;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectCrop extends CommandSelectModify
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "crop";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.selectWand.usage");
    }

    @Override
    public void executeSelection(ICommandSender sender, SelectionOwner selectionOwner, String[] args) throws CommandException
    {
        World world = sender.getEntityWorld();
        BlockArea area = selectionOwner.getSelection();

        String exp = args.length > 0 ? buildString(args, 0) : "is:air";
        PositionedBlockMatcher matcher = new PositionedBlockMatcher(RecurrentComplex.specialRegistry, exp);

        for (EnumFacing direction : EnumFacing.VALUES)
            while (area != null && CommandSelectWand.sideStream(area, direction).allMatch(p -> matcher.test(PositionedBlockMatcher.Argument.at(world, p))))
                area = BlockAreas.shrink(area, direction, 1);

        selectionOwner.setSelection(area);
    }
}
