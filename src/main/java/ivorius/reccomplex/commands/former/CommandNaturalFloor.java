/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.former;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockSurfaceArea;
import ivorius.ivtoolkit.blocks.BlockSurfacePos;
import ivorius.ivtoolkit.world.MockWorld;
import ivorius.reccomplex.block.RCBlocks;
import ivorius.reccomplex.commands.CommandVirtual;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.commands.parameters.*;
import ivorius.reccomplex.commands.rcparameters.RCExpect;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandNaturalFloor extends CommandExpecting implements CommandVirtual
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
        return "floor";
    }

    @Override
    public Expect<?> expect()
    {
        return RCExpect.expectRC()
                .named("expansion", "e").any("0", "1", "2");
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MockWorld world, ICommandSender sender, String[] args) throws CommandException
    {
        Parameters parameters = Parameters.of(args, expect()::declare);

        BlockArea area = RCCommands.getSelectionOwner(sender, null, true).getSelection();
        double expandFloor = parameters.get("expansion").to(NaP::asDouble).optional().orElse(1.);

        placeNaturalFloor(world, area, expandFloor);
    }
}
