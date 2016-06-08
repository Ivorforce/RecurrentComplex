/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import net.minecraft.util.BlockPos;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.ivtoolkit.blocks.BlockAreas;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.block.material.Material;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;

import java.util.stream.StreamSupport;

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
        return ServerTranslations.usage("commands.selectCrop.usage");
    }

    @Override
    public void processCommandSelection(EntityPlayerMP player, StructureEntityInfo structureEntityInfo, BlockPos point1, BlockPos point2, String[] args)
    {
        World world = player.getEntityWorld();
        BlockArea area = new BlockArea(point1, point2);

        for (EnumFacing direction : EnumFacing.VALUES)
            while (area != null && isSideEmpty(world, area, direction))
                area = BlockAreas.shrink(area, direction, 1);

        structureEntityInfo.setSelection(area);
        structureEntityInfo.sendSelectionToClients(player);
    }

    public static boolean isSideEmpty(final World world, BlockArea area, EnumFacing direction)
    {
        return StreamSupport.stream(BlockAreas.side(area, direction).spliterator(), false).allMatch(coord -> world.getBlockState(coord).getBlock().getMaterial() == Material.air);
    }
}
