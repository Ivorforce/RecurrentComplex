/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.blocks.RCBlocks;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.utils.BlockAreas;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        return "commands.selectNatural.usage";
    }

    @Override
    public void processCommandSelection(EntityPlayerMP player, StructureEntityInfo structureEntityInfo, BlockCoord point1, BlockCoord point2, String[] args)
    {
        World world = player.getEntityWorld();

        BlockArea area = new BlockArea(point1, point2);
        double expandFloor = args.length >= 1 ? parseDouble(player, args[0]) : 1;

        placeNaturalFloor(world, area, expandFloor);
        placeNaturalAir(world, area);
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, "0", "1", "2");

        return super.addTabCompletionOptions(commandSender, args);
    }

    public static void placeNaturalFloor(World world, BlockArea area, double lowerExpansion)
    {
        lowerExpansion += 0.01; // Rounding and stuff

        Block floorBlock = RCBlocks.naturalFloor;
        Block airBlock1 = RCBlocks.negativeSpace;

        BlockCoord lowerPoint = area.getLowerCorner();
        BlockCoord higherPoint = area.getHigherCorner();

        Set<BlockCoord> stopped = new HashSet<>();

        for (int y = lowerPoint.y; y <= higherPoint.y; y++)
        {
            for (BlockCoord surfaceCoord : BlockAreas.side(area, ForgeDirection.DOWN))
            {
                if (!stopped.contains(surfaceCoord))
                {
                    Block block = world.getBlock(surfaceCoord.x, y, surfaceCoord.z);

                    if ((block.getMaterial() != Material.air && block != airBlock1))
                    {
                        if (block.isNormalCube(world, surfaceCoord.x, y, surfaceCoord.z) && block != floorBlock && y > lowerPoint.y)
                        {
                            setBlockIfAirInArea(world, new BlockCoord(surfaceCoord.x, y - 1, surfaceCoord.z), floorBlock, area);

                            fillSurface(world, area, lowerExpansion, floorBlock, surfaceCoord, y);
                        }

                        stopped.add(surfaceCoord);
                    }
                }
            }
        }
    }

    private static void fillSurface(World world, BlockArea area, double expansion, Block floorBlock, BlockCoord surfaceCoord, int y)
    {
        for (int expX = MathHelper.ceiling_double_int(-expansion); expX <= expansion; expX++)
        {
            for (int expZ = MathHelper.ceiling_double_int(-expansion); expZ <= expansion; expZ++)
            {
                if (expX * expX + expZ * expZ <= expansion * expansion)
                    setBlockIfAirInArea(world, new BlockCoord(surfaceCoord.x + expX, y - 1, surfaceCoord.z + expZ), floorBlock, area);
            }
        }
    }

    public static void setBlockIfAirInArea(World world, BlockCoord coord, Block block, BlockArea area)
    {
        if (area.contains(coord))
        {
            Block prevBlock = world.getBlock(coord.x, coord.y, coord.z);
            if (prevBlock.getMaterial() == Material.air || prevBlock == RCBlocks.negativeSpace)
                world.setBlock(coord.x, coord.y, coord.z, block);
        }
    }

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

    public static void placeNaturalAir(World world, BlockArea area)
    {
        Block spaceBlock = RCBlocks.negativeSpace;

        BlockCoord lowerPoint = area.getLowerCorner();
        BlockCoord higherPoint = area.getHigherCorner();

        for (BlockCoord surfaceCoord : BlockAreas.side(area, ForgeDirection.DOWN))
        {
            int safePoint = lowerPoint.y;

            for (int y = higherPoint.y; y >= lowerPoint.y; y--)
            {
                Block block = world.getBlock(surfaceCoord.x, y, surfaceCoord.z);

                if ((block.getMaterial() != Material.air && block != spaceBlock) || sidesClosed(world, new BlockCoord(surfaceCoord.x, y, surfaceCoord.z), area) >= 3)
                {
                    safePoint = y + (block == RCBlocks.naturalFloor ? 1 : 3);
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

                    if ((block.getMaterial() != Material.air && block != spaceBlock) || sidesClosed(world, new BlockCoord(surfaceCoord.x, y, surfaceCoord.z), area) >= 3)
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
}
