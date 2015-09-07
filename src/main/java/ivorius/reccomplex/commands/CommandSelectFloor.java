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
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectFloor extends CommandSelectModify
{
    public static void placeNaturalFloor(World world, BlockArea area, double lowerExpansion)
    {
        lowerExpansion += 0.01; // Rounding and stuff

        Block floorBlock = RCBlocks.genericSolid;
        Block airBlock1 = RCBlocks.genericSpace;

        BlockCoord lowerPoint = area.getLowerCorner();
        BlockCoord higherPoint = area.getHigherCorner();

        Set<BlockCoord> stopped = new HashSet<>();
        Set<BlockCoord> stopping = new HashSet<>();

        for (int y = lowerPoint.y + 1; y <= higherPoint.y; y++)
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

                            fillSurface(world, area, lowerExpansion, floorBlock, surfaceCoord, y, stopping);
                        }
                    }
                }
            }

            stopped.addAll(stopping);
            stopping.clear();
        }
    }

    private static void fillSurface(World world, BlockArea area, double expansion, Block floorBlock, BlockCoord surfaceCoord, int y, Set<BlockCoord> coords)
    {
        for (int expX = MathHelper.ceiling_double_int(-expansion); expX <= expansion; expX++)
        {
            for (int expZ = MathHelper.ceiling_double_int(-expansion); expZ <= expansion; expZ++)
            {
                if (expX * expX + expZ * expZ <= expansion * expansion)
                {
                    BlockCoord coord = new BlockCoord(surfaceCoord.x + expX, y - 1, surfaceCoord.z + expZ);
                    setBlockIfAirInArea(world, coord, floorBlock, area);
                    coords.add(coord);
                }
            }
        }
    }

    public static void setBlockIfAirInArea(World world, BlockCoord coord, Block block, BlockArea area)
    {
        if (area.contains(coord))
        {
            Block prevBlock = world.getBlock(coord.x, coord.y, coord.z);
            if (prevBlock.getMaterial() == Material.air || prevBlock == RCBlocks.genericSpace)
                world.setBlock(coord.x, coord.y, coord.z, block);
        }
    }

    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "floor";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.selectFloor.usage");
    }

    @Override
    public void processCommandSelection(EntityPlayerMP player, StructureEntityInfo structureEntityInfo, BlockCoord point1, BlockCoord point2, String[] args)
    {
        World world = player.getEntityWorld();

        BlockArea area = new BlockArea(point1, point2);
        double expandFloor = args.length >= 1 ? parseDouble(player, args[0]) : 1;

        placeNaturalFloor(world, area, expandFloor);
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, "0", "1", "2");

        return super.addTabCompletionOptions(commandSender, args);
    }
}
