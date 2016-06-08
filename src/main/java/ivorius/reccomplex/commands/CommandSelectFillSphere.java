/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import net.minecraft.command.CommandException;
import net.minecraft.util.BlockPos;
import ivorius.ivtoolkit.math.IvShapeHelper;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.entities.StructureEntityInfo;
import net.minecraft.block.state.IBlockState;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectFillSphere extends CommandSelectModify
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "sphere";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.selectFillSphere.usage");
    }

    @Override
    public void processCommandSelection(EntityPlayerMP player, StructureEntityInfo structureEntityInfo, BlockPos point1, BlockPos point2, String[] args) throws CommandException
    {
        if (args.length >= 1)
        {
            World world = player.getEntityWorld();

            Block dstBlock = getBlockByText(player, args[0]);
            int[] dstMeta = args.length >= 2 ? getMetadatas(args[1]) : new int[]{0};
            List<IBlockState> dst = IntStream.of(dstMeta).mapToObj(dstBlock::getStateFromMeta).collect(Collectors.toList());

            BlockArea area = new BlockArea(point1, point2);

            double[] spheroidOrigin = new double[]{(point1.getX() + point2.getX()) * 0.5, (point1.getY() + point2.getY()) * 0.5, (point1.getZ() + point2.getZ()) * 0.5};
            int[] areaSize = area.areaSize();
            double[] spheroidSize = new double[]{areaSize[0] * 0.5, areaSize[1] * 0.5, areaSize[2] * 0.5};

            for (BlockPos coord : area)
            {
                double[] coordPoint = new double[]{coord.getX(), coord.getY(), coord.getZ()};
                if (IvShapeHelper.isPointInSpheroid(coordPoint, spheroidOrigin, spheroidSize))
                {
                    IBlockState state = dst.get(player.getRNG().nextInt(dst.size()));
                    world.setBlockState(coord, state, 3);
                }
            }
        }
        else
        {
            throw ServerTranslations.wrongUsageException("commands.selectFillSphere.usage");
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, Block.blockRegistry.getKeys());
        else if (args.length == 2)
        {
            return getListOfStringsMatchingLastWord(args, "0");
        }

        return null;
    }
}
