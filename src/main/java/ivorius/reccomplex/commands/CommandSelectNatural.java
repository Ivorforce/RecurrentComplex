/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.reccomplex.blocks.RCBlocks;
import ivorius.reccomplex.entities.StructureEntityInfo;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectNatural extends CommandSelectModify
{
    @Override
    public String getCommandName()
    {
        return "selectNatural";
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
        {
            return getListOfStringsMatchingLastWord(args, "0", "1", "2");
        }

        return super.addTabCompletionOptions(commandSender, args);
    }

    public static void placeNaturalFloor(World world, BlockArea area, double lowerExpansion)
    {
        lowerExpansion += 0.01; // Rounding and stuff

        Block floorBlock = RCBlocks.naturalFloor;
        Block airBlock1 = RCBlocks.negativeSpace;

        BlockCoord lowerPoint = area.getLowerCorner();
        BlockCoord higherPoint = area.getHigherCorner();

        for (int x = lowerPoint.x; x <= higherPoint.x; x++)
        {
            for (int z = lowerPoint.z; z <= higherPoint.z; z++)
            {
                for (int y = lowerPoint.y; y <= higherPoint.y; y++)
                {
                    Block block = world.getBlock(x, y, z);

                    if ((block.getMaterial() != Material.air && block != floorBlock && block != airBlock1))
                    {
                        if (block.isNormalCube(world, x, y, z) && y > lowerPoint.y)
                        {
                            setBlockIfAirInArea(world, new BlockCoord(x, y - 1, z), floorBlock, area);

                            for (int expX = MathHelper.ceiling_double_int(-lowerExpansion); expX <= lowerExpansion; expX++)
                            {
                                for (int expZ = MathHelper.ceiling_double_int(-lowerExpansion); expZ <= lowerExpansion; expZ++)
                                {
                                    if (expX * expX + expZ * expZ <= lowerExpansion * lowerExpansion)
                                    {
                                        setBlockIfAirInArea(world, new BlockCoord(x + expX, y - 1, z + expZ), floorBlock, area);
                                    }
                                }
                            }
                        }

                        break;
                    }
                }
            }
        }
    }

    public static void setBlockIfAirInArea(World world, BlockCoord coord, Block block, BlockArea area)
    {
        if (area.contains(coord))
        {
            Block prevBlock = world.getBlock(coord.x, coord.y, coord.z);
            if (prevBlock.getMaterial() == Material.air || prevBlock == RCBlocks.negativeSpace)
            {
                world.setBlock(coord.x, coord.y, coord.z, block);
            }
        }
    }

    public static int sidesClosed(World world, BlockCoord coord, BlockArea area)
    {
        int sides = 0;

        BlockCoord lower = area.getLowerCorner();
        BlockCoord higher = area.getHigherCorner();

        if (sideClosed(world, new BlockCoord(lower.x, coord.y, coord.z), coord.x - lower.x, 1, 0, 0))
        {
            sides++;
        }
        if (sideClosed(world, new BlockCoord(higher.x, coord.y, coord.z), higher.x - coord.x, -1, 0, 0))
        {
            sides++;
        }
        if (sideClosed(world, new BlockCoord(coord.x, coord.y, lower.z), coord.z - lower.z, 0, 0, 1))
        {
            sides++;
        }
        if (sideClosed(world, new BlockCoord(coord.x, coord.y, higher.z), higher.z - coord.z, 0, 0, -1))
        {
            sides++;
        }

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
            {
                return true;
            }
        }

        return false;
    }

    public static void placeNaturalAir(World world, BlockArea area)
    {
        Block spaceBlock = RCBlocks.negativeSpace;

        BlockCoord lowerPoint = area.getLowerCorner();
        BlockCoord higherPoint = area.getHigherCorner();

        for (int x = lowerPoint.x; x <= higherPoint.x; x++)
        {
            for (int z = lowerPoint.z; z <= higherPoint.z; z++)
            {
                int safePoint = lowerPoint.y;

                for (int y = higherPoint.y; y >= lowerPoint.y; y--)
                {
                    Block block = world.getBlock(x, y, z);

                    if ((block.getMaterial() != Material.air && block != spaceBlock) || sidesClosed(world, new BlockCoord(x, y, z), area) >= 3)
                    {
                        safePoint = y + (block == RCBlocks.naturalFloor ? 1 : 3);
                        break;
                    }
                }

                for (int y = safePoint; y <= higherPoint.y; y++)
                {
                    world.setBlock(x, y, z, spaceBlock);
                }

                if (safePoint > lowerPoint.y)
                {
                    for (int y = lowerPoint.y; y <= higherPoint.y; y++)
                    {
                        Block block = world.getBlock(x, y, z);

                        if ((block.getMaterial() != Material.air && block != spaceBlock) || sidesClosed(world, new BlockCoord(x, y, z), area) >= 3)
                        {
                            safePoint = y - 1;
                            break;
                        }
                    }
                }

                for (int y = lowerPoint.y; y <= safePoint; y++)
                {
                    world.setBlock(x, y, z, spaceBlock);
                }
            }
        }
    }
}
