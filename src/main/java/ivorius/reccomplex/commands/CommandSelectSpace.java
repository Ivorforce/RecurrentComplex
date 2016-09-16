/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.blocks.BlockGenericSpace;
import ivorius.reccomplex.blocks.RCBlocks;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.utils.BlockSurfaceArea;
import ivorius.reccomplex.utils.BlockSurfacePos;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectSpace extends CommandSelectModify
{
    public static int sidesClosed(World world, BlockPos coord, BlockArea area)
    {
        int sides = 0;

        BlockPos lower = area.getLowerCorner();
        BlockPos higher = area.getHigherCorner();

        if (sideClosed(world, new BlockPos(lower.getX(), coord.getY(), coord.getZ()), coord.getX() - lower.getX(), 1, 0, 0))
            sides++;
        if (sideClosed(world, new BlockPos(higher.getX(), coord.getY(), coord.getZ()), higher.getX() - coord.getX(), -1, 0, 0))
            sides++;
        if (sideClosed(world, new BlockPos(coord.getX(), coord.getY(), lower.getZ()), coord.getZ() - lower.getZ(), 0, 0, 1))
            sides++;
        if (sideClosed(world, new BlockPos(coord.getX(), coord.getY(), higher.getZ()), higher.getZ() - coord.getZ(), 0, 0, -1))
            sides++;

        return sides;
    }

    public static boolean sideClosed(World world, BlockPos coord, int iterations, int xP, int yP, int zP)
    {
        for (int i = 0; i < iterations; i++)
        {
            BlockPos pos = coord.add(xP * i, yP * i, zP * i);
            IBlockState blockState = world.getBlockState(pos);

            if (!blockState.getBlock().isReplaceable(world, pos))
                return true;
        }

        return false;
    }

    public static void placeNaturalAir(World world, BlockArea area, int floorDistance, int maxClosedSides)
    {
        BlockGenericSpace spaceBlock = RCBlocks.genericSpace;

        BlockPos lowerPoint = area.getLowerCorner();
        BlockPos higherPoint = area.getHigherCorner();

        Set<BlockPos> set = new HashSet<>();

        for (BlockSurfacePos surfaceCoord : BlockSurfaceArea.from(area))
        {
            int safePoint = lowerPoint.getY();

            for (int y = higherPoint.getY(); y >= lowerPoint.getY(); y--)
            {
                IBlockState blockState = world.getBlockState(surfaceCoord.blockPos(y));

                if ((blockState.getMaterial() != Material.AIR && blockState.getBlock() != spaceBlock) || sidesClosed(world, surfaceCoord.blockPos(y), area) >= maxClosedSides)
                {
                    boolean isFloor = blockState == RCBlocks.genericSolid.getDefaultState();
                    safePoint = y + (isFloor ? 1 : floorDistance);
                    break;
                }
            }

            for (int y = safePoint; y <= higherPoint.getY(); y++)
                set.add(surfaceCoord.blockPos(y));

            if (safePoint > lowerPoint.getY())
            {
                for (int y = lowerPoint.getY(); y <= higherPoint.getY(); y++)
                {
                    IBlockState blockState = world.getBlockState(surfaceCoord.blockPos(y));

                    if ((blockState.getMaterial() != Material.AIR && blockState.getBlock() != spaceBlock) || sidesClosed(world, surfaceCoord.blockPos(y), area) >= maxClosedSides)
                    {
                        safePoint = y - 1;
                        break;
                    }
                }
            }

            for (int y = lowerPoint.getY(); y <= safePoint; y++)
                set.add(surfaceCoord.blockPos(y));
        }

        set.forEach(pos -> {
            BlockPos down = pos.down();
            BlockPos down2 = pos.down(2);
            world.setBlockState(pos,
                    pos.getY() > lowerPoint.getY() && !set.contains(down)
                    && world.getBlockState(down).getBlock().isReplaceable(world, down) && world.getBlockState(down2).getBlock().isReplaceable(world, down2)
                    && new BlockArea(pos.subtract(new Vec3i(2, 0, 2)), pos.add(new Vec3i(2, 0, 2))).stream().allMatch(set::contains)
                    ? spaceBlock.getDefaultState().withProperty(BlockGenericSpace.TYPE, 1)
                    : spaceBlock.getDefaultState()
            );
        });
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
    public void executeSelection(EntityPlayerMP player, StructureEntityInfo structureEntityInfo, BlockPos point1, BlockPos point2, String[] args) throws NumberInvalidException
    {
        World world = player.getEntityWorld();

        BlockArea area = new BlockArea(point1, point2);

        int floorDistance = (args.length >= 1 ? parseInt(args[0]) : 0) + 1;
        int maxClosedSides = args.length >= 2 ? parseInt(args[1]) : 3;

        placeNaturalAir(world, area, floorDistance, maxClosedSides);
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, "3", "2", "1");
        else if (args.length == 2)
            return getListOfStringsMatchingLastWord(args, "3", "4", "5");

        return super.getTabCompletionOptions(server, sender, args, pos);
    }
}
