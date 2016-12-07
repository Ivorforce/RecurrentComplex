/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockAreas;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.utils.expression.PositionedBlockMatcher;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectWand extends CommandBase
{
    @Nonnull
    protected static Stream<BlockPos> sideStream(BlockArea area, EnumFacing direction)
    {
        return StreamSupport.stream(BlockAreas.side(area, direction).spliterator(), false);
    }

    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "wand";
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

        boolean changed = true;
        int total = 0;

        while (changed)
        {
            changed = false;

            String exp = args.length > 0 ? buildString(args, 0) : "!is:air";
            PositionedBlockMatcher matcher = new PositionedBlockMatcher(RecurrentComplex.specialRegistry, exp);

            for (EnumFacing direction : EnumFacing.VALUES)
            {
                BlockArea expand;

                while (sideStream((expand = BlockAreas.expand(area, direction, 1)), direction).anyMatch(p -> matcher.test(PositionedBlockMatcher.Argument.at(world, p))) && (total++) < 300)
                {
                    area = expand;
                    changed = true;
                }
            }
        }

        selectionOwner.setSelection(area);
    }
}
