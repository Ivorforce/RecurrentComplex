/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockAreas;
import ivorius.ivtoolkit.world.MockWorld;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.commands.parameters.RCExpect;
import ivorius.reccomplex.commands.parameters.RCParameters;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.utils.expression.PositionedBlockExpression;
import ivorius.reccomplex.utils.optional.IvOptional;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectCrop extends CommandVirtual
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

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    void execute(MockWorld world, ICommandSender commandSender, String[] args) throws CommandException
    {
        SelectionOwner selectionOwner = RCCommands.getSelectionOwner(commandSender, null, true);
        BlockArea area = selectionOwner.getSelection();

        RCParameters parameters = RCParameters.of(args);

        PositionedBlockExpression matcher = new PositionedBlockExpression(RecurrentComplex.specialRegistry);
        IvOptional.ifAbsent(parameters.rc().expression(matcher).optional(), () -> matcher.setExpression("is:air"));

        for (EnumFacing direction : EnumFacing.VALUES)
            while (area != null && CommandSelectWand.sideStream(area, direction).allMatch(p -> matcher.test(PositionedBlockExpression.Argument.at(world, p))))
                area = BlockAreas.shrink(area, direction, 1);

        selectionOwner.setSelection(area);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        return RCExpect.startRC()
                .block()
                .get(server, sender, args, targetPos);
    }
}
