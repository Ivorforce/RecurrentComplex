/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.utils.BlockAreas;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;

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
        return "commands.selectCrop.usage";
    }

    @Override
    public void processCommandSelection(EntityPlayerMP player, StructureEntityInfo structureEntityInfo, BlockCoord point1, BlockCoord point2, String[] args)
    {
        World world = player.getEntityWorld();
        BlockArea area = new BlockArea(point1, point2);

        for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
            while (area != null && isSideEmpty(world, area, direction))
                area = BlockAreas.shrink(area, direction, 1);

        if (area != null)
        {
            structureEntityInfo.selectedPoint1 = area.getPoint1();
            structureEntityInfo.selectedPoint2 = area.getPoint2();
        }
        else
        {
            structureEntityInfo.selectedPoint1 = null;
            structureEntityInfo.selectedPoint2 = null;
        }

        structureEntityInfo.sendSelectionToClients(player);
    }

    public static boolean isSideEmpty(final World world, BlockArea area, ForgeDirection direction)
    {
        return Iterables.all(BlockAreas.side(area, direction), new Predicate<BlockCoord>()
        {
            @Override
            public boolean apply(BlockCoord coord)
            {
                return coord.getBlock(world).getMaterial() == Material.air;
            }
        });
    }
}
