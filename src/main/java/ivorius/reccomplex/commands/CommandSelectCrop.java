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
import ivorius.reccomplex.utils.algebra.ExpressionCache;
import ivorius.reccomplex.utils.expression.PositionedBlockMatcher;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectCrop extends CommandBase
{
    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "crop";
    }

    @Override
    public String getUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.selectWand.usage");
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        SelectionOwner selectionOwner = RCCommands.getSelectionOwner(commandSender, null, true);
        World world = commandSender.getEntityWorld();
        BlockArea area = selectionOwner.getSelection();

        String exp = args.length > 0 ? buildString(args, 0) : "is:air";
        PositionedBlockMatcher matcher = ExpressionCache.of(new PositionedBlockMatcher(RecurrentComplex.specialRegistry), exp);
        RCCommands.ensureValid(matcher, 0);

        for (EnumFacing direction : EnumFacing.VALUES)
            while (area != null && CommandSelectWand.sideStream(area, direction).allMatch(p -> matcher.test(PositionedBlockMatcher.Argument.at(world, p))))
                area = BlockAreas.shrink(area, direction, 1);

        selectionOwner.setSelection(area);
    }
}
