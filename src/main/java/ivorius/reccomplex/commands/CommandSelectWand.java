/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockAreas;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.utils.RCBlockAreas;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.block.material.Material;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.stream.StreamSupport;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectWand extends CommandSelectModify
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "wand";
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

        boolean changed = true;
        int total = 0;

        while(changed)
        {
            changed = false;

            for (EnumFacing direction : EnumFacing.VALUES)
            {
                BlockArea expand;
                while (!isSideEmpty(world, (expand = RCBlockAreas.expand(area, direction, 1)), direction) && (total ++) < 300)
                {
                    area = expand;
                    changed = true;
                }
            }
        }

        structureEntityInfo.setSelection(area);
        structureEntityInfo.sendSelectionToClients(player);
    }

    public static boolean isSideEmpty(final World world, BlockArea area, EnumFacing direction)
    {
        return StreamSupport.stream(BlockAreas.side(area, direction).spliterator(), false).allMatch(coord -> world.getBlockState(coord).getMaterial() == Material.AIR);
    }
}
