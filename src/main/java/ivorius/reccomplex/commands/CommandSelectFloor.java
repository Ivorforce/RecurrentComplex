/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockAreas;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.util.BlockPos;
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
import net.minecraft.util.EnumFacing;

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

        IBlockState floorBlock = RCBlocks.genericSolid.getDefaultState();
        Block airBlock1 = RCBlocks.genericSpace;

        BlockPos lowerPoint = area.getLowerCorner();
        BlockPos higherPoint = area.getHigherCorner();

        Set<BlockPos> stopped = new HashSet<>();
        Set<BlockPos> stopping = new HashSet<>();

        for (int y = lowerPoint.getY() + 1; y <= higherPoint.getY(); y++)
        {
            for (BlockPos surfaceCoord : BlockAreas.side(area, EnumFacing.DOWN))
            {
                if (!stopped.contains(surfaceCoord))
                {
                    IBlockState block = world.getBlockState(new BlockPos(surfaceCoord.getX(), y, surfaceCoord.getZ()));

                    if ((block.getBlock().getMaterial() != Material.air && block.getBlock() != airBlock1))
                    {
                        if (block.getBlock().isNormalCube(world, new BlockPos(surfaceCoord.getX(), y, surfaceCoord.getZ())) && block != floorBlock && y > lowerPoint.getY())
                        {
                            setBlockIfAirInArea(world, new BlockPos(surfaceCoord.getX(), y - 1, surfaceCoord.getZ()), floorBlock, area);

                            fillSurface(world, area, lowerExpansion, floorBlock, surfaceCoord, y, stopping);
                        }
                    }
                }
            }

            stopped.addAll(stopping);
            stopping.clear();
        }
    }

    private static void fillSurface(World world, BlockArea area, double expansion, IBlockState floorBlock, BlockPos surfaceCoord, int y, Set<BlockPos> coords)
    {
        for (int expX = MathHelper.ceiling_double_int(-expansion); expX <= expansion; expX++)
        {
            for (int expZ = MathHelper.ceiling_double_int(-expansion); expZ <= expansion; expZ++)
            {
                if (expX * expX + expZ * expZ <= expansion * expansion)
                {
                    BlockPos coord = new BlockPos(surfaceCoord.getX() + expX, y - 1, surfaceCoord.getZ() + expZ);
                    setBlockIfAirInArea(world, coord, floorBlock, area);
                    coords.add(coord);
                }
            }
        }
    }

    public static void setBlockIfAirInArea(World world, BlockPos coord, IBlockState block, BlockArea area)
    {
        if (area.contains(coord))
        {
            IBlockState prevBlock = world.getBlockState(coord);
            if (prevBlock.getBlock().getMaterial() == Material.air || prevBlock.getBlock() == RCBlocks.genericSpace)
                world.setBlockState(coord, block);
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
    public void processCommandSelection(EntityPlayerMP player, StructureEntityInfo structureEntityInfo, BlockPos point1, BlockPos point2, String[] args) throws NumberInvalidException
    {
        World world = player.getEntityWorld();

        BlockArea area = new BlockArea(point1, point2);
        double expandFloor = args.length >= 1 ? parseDouble(args[0]) : 1;

        placeNaturalFloor(world, area, expandFloor);
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, "0", "1", "2");

        return super.addTabCompletionOptions(commandSender, args, pos);
    }
}
