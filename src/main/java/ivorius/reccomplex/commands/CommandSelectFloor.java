/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockSurfaceArea;
import ivorius.ivtoolkit.blocks.BlockSurfacePos;
import ivorius.ivtoolkit.world.MockWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.block.RCBlocks;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectFloor extends CommandVirtual
{
    public static void placeNaturalFloor(MockWorld world, BlockArea area, double lowerExpansion)
    {
        lowerExpansion += 0.01; // Rounding and stuff

        IBlockState floorBlock = RCBlocks.genericSolid.getDefaultState();
        Block airBlock1 = RCBlocks.genericSpace;

        BlockPos lowerPoint = area.getLowerCorner();
        BlockPos higherPoint = area.getHigherCorner();

        Set<BlockSurfacePos> stopped = new HashSet<>();
        Set<BlockSurfacePos> stopping = new HashSet<>();

        for (int y = lowerPoint.getY() + 1; y <= higherPoint.getY(); y++)
        {
            for (BlockSurfacePos surfaceCoord : (Iterable<BlockSurfacePos>) BlockSurfaceArea.from(area).stream().filter((o) -> !stopped.contains(o))::iterator)
            {
                IBlockState block = world.getBlockState(surfaceCoord.blockPos(y));

                if ((block.getMaterial() != Material.AIR && block.getBlock() != airBlock1))
                {
                    stopping.add(surfaceCoord);

                    if (block != floorBlock)
                    {
                        setBlockIfAirInArea(world, surfaceCoord.blockPos(y - 1), floorBlock, area);

                        fillSurface(world, area, lowerExpansion, floorBlock, surfaceCoord.blockPos(y), stopping);
                    }
                }
            }

            stopped.addAll(stopping);
            stopping.clear();
        }
    }

    private static void fillSurface(MockWorld world, BlockArea area, double expansion, IBlockState floorBlock, BlockPos pos, Set<BlockSurfacePos> coords)
    {
        BlockSurfacePos surfacePos = BlockSurfacePos.from(pos);

        for (int expX = MathHelper.ceil(-expansion); expX <= expansion; expX++)
        {
            for (int expZ = MathHelper.ceil(-expansion); expZ <= expansion; expZ++)
            {
                if (expX * expX + expZ * expZ <= expansion * expansion)
                {
                    BlockSurfacePos scoord = surfacePos.add(expX, expZ);
                    setBlockIfAirInArea(world, scoord.blockPos(pos.getY()), floorBlock, area);
                    coords.add(scoord);
                }
            }
        }
    }

    public static void setBlockIfAirInArea(MockWorld world, BlockPos coord, IBlockState block, BlockArea area)
    {
        if (area.contains(coord))
        {
            IBlockState prevBlock = world.getBlockState(coord);
            if (prevBlock.getMaterial() == Material.AIR || prevBlock.getBlock() == RCBlocks.genericSpace)
                world.setBlockState(coord, block);
        }
    }

    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "floor";
    }

    @Override
    public String getUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.selectFloor.usage");
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, "0", "1", "2");

        return super.getTabCompletions(server, sender, args, pos);
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MockWorld world, ICommandSender commandSender, String[] args) throws CommandException
    {
        BlockArea area = RCCommands.getSelectionOwner(commandSender, null, true).getSelection();
        double expandFloor = args.length >= 1 ? parseDouble(args[0]) : 1;

        placeNaturalFloor(world, area, expandFloor);
    }
}
