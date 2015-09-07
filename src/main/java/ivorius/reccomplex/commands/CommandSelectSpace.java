/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockAreas;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.blocks.RCBlocks;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectSpace extends CommandSelectModify
{
    public static int sidesClosed(World world, BlockCoord coord, BlockArea area)
    {
        int sides = 0;

        BlockCoord lower = area.getLowerCorner();
        BlockCoord higher = area.getHigherCorner();

        if (sideClosed(world, new BlockCoord(lower.x, coord.y, coord.z), coord.x - lower.x, 1, 0, 0))
            sides++;
        if (sideClosed(world, new BlockCoord(higher.x, coord.y, coord.z), higher.x - coord.x, -1, 0, 0))
            sides++;
        if (sideClosed(world, new BlockCoord(coord.x, coord.y, lower.z), coord.z - lower.z, 0, 0, 1))
            sides++;
        if (sideClosed(world, new BlockCoord(coord.x, coord.y, higher.z), higher.z - coord.z, 0, 0, -1))
            sides++;

        return sides;
    }

    public static boolean sideClosed(World world, BlockCoord coord, int iterations, int xP, int yP, int zP)
    {
        for (int i = 0; i < iterations; i++)
        {
            int x = coord.x + xP * i;
            int y = coord.y + yP * i;
            int z = coord.z + zP * i;
            Block block = world.getBlock(x, y, z);

            if (!block.isReplaceable(world, x, y, z))
                return true;
        }

        return false;
    }

    public static void placeNaturalAir(World world, BlockArea area, int floorDistance, int maxClosedSides)
    {
        Block spaceBlock = RCBlocks.genericSpace;

        BlockCoord lowerPoint = area.getLowerCorner();
        BlockCoord higherPoint = area.getHigherCorner();

        for (BlockCoord surfaceCoord : BlockAreas.side(area, ForgeDirection.DOWN))
        {
            int safePoint = lowerPoint.y;

            for (int y = higherPoint.y; y >= lowerPoint.y; y--)
            {
                Block block = world.getBlock(surfaceCoord.x, y, surfaceCoord.z);

                if ((block.getMaterial() != Material.air && block != spaceBlock) || sidesClosed(world, new BlockCoord(surfaceCoord.x, y, surfaceCoord.z), area) >= maxClosedSides)
                {
                    safePoint = y + (block == RCBlocks.genericSolid ? 1 : floorDistance);
                    break;
                }
            }

            for (int y = safePoint; y <= higherPoint.y; y++)
                world.setBlock(surfaceCoord.x, y, surfaceCoord.z, spaceBlock);

            if (safePoint > lowerPoint.y)
            {
                for (int y = lowerPoint.y; y <= higherPoint.y; y++)
                {
                    Block block = world.getBlock(surfaceCoord.x, y, surfaceCoord.z);

                    if ((block.getMaterial() != Material.air && block != spaceBlock) || sidesClosed(world, new BlockCoord(surfaceCoord.x, y, surfaceCoord.z), area) >= maxClosedSides)
                    {
                        safePoint = y - 1;
                        break;
                    }
                }
            }

            for (int y = lowerPoint.y; y <= safePoint; y++)
                world.setBlock(surfaceCoord.x, y, surfaceCoord.z, spaceBlock);
        }
    }

    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "space";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.selectSpace.usage");
    }

    @Override
    public void processCommandSelection(EntityPlayerMP player, StructureEntityInfo structureEntityInfo, BlockCoord point1, BlockCoord point2, String[] args)
    {
        World world = player.getEntityWorld();

        BlockArea area = new BlockArea(point1, point2);

        int floorDistance = args.length >= 1 ? parseInt(player, args[0]) : 3;
        int maxClosedSides = args.length >= 2 ? parseInt(player, args[1]) : 3;

        placeNaturalAir(world, area, floorDistance, maxClosedSides);
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, "3", "2", "1");
        else if (args.length == 2)
            return getListOfStringsMatchingLastWord(args, "3", "4", "5");

        return super.addTabCompletionOptions(commandSender, args);
    }
}
