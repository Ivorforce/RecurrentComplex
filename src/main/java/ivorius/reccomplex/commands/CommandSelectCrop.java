/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.structures.generic.matchers.PositionedBlockMatcher;
import net.minecraft.util.math.BlockPos;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.ivtoolkit.blocks.BlockAreas;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;

import java.util.function.Predicate;

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
    public void executeSelection(EntityPlayerMP player, StructureEntityInfo structureEntityInfo, BlockPos point1, BlockPos point2, String[] args)
    {
        World world = player.getEntityWorld();
        BlockArea area = new BlockArea(point1, point2);

        String exp = args.length > 0 ? buildString(args, 0) : "is.air";
        PositionedBlockMatcher matcher = new PositionedBlockMatcher(RecurrentComplex.specialRegistry, exp);

        for (EnumFacing direction : EnumFacing.VALUES)
            while (area != null && CommandSelectWand.sideStream(area, direction).allMatch(p -> matcher.test(PositionedBlockMatcher.Argument.at(world, p))))
                area = BlockAreas.shrink(area, direction, 1);

        structureEntityInfo.setSelection(area);
        structureEntityInfo.sendSelectionToClients(player);
    }

}
