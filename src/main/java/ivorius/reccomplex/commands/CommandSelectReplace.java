/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import net.minecraft.command.CommandException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.util.BlockPos;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.entities.StructureEntityInfo;
import net.minecraft.block.state.IBlockState;
import ivorius.reccomplex.utils.BlockStates;
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
public class CommandSelectReplace extends CommandSelectModify
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "replace";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.selectReplace.usage");
    }

    @Override
    public void processCommandSelection(EntityPlayerMP player, StructureEntityInfo structureEntityInfo, BlockPos point1, BlockPos point2, String[] args) throws CommandException
    {
        if (args.length >= 2)
        {
            World world = player.getEntityWorld();

            Block src = getBlockByText(player, args[0]);
            int[] srcMeta = args.length >= 3 ? getMetadatas(args[2]) : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};

            Block dstBlock = getBlockByText(player, args[1]);
            int[] dstMeta = args.length >= 4 ? getMetadatas(args[1]) : new int[]{0};
            List<IBlockState> dst = IntStream.of(dstMeta).mapToObj(dstBlock::getStateFromMeta).collect(Collectors.toList());

            for (BlockPos coord : new BlockArea(point1, point2))
            {
                IBlockState prev = world.getBlockState(coord);

                boolean correctMeta = IntStream.of(srcMeta).anyMatch(i -> i == BlockStates.toMetadata(prev));

                if (correctMeta && src == prev.getBlock())
                {
                    IBlockState state = dst.get(player.getRNG().nextInt(dst.size()));
                    world.setBlockState(coord, state, 3);
                }
            }
        }
        else
        {
            throw ServerTranslations.wrongUsageException("commands.selectReplace.usage");
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args, BlockPos pos)
    {
        if (args.length == 1 || args.length == 2)
            return getListOfStringsMatchingLastWord(args, Block.blockRegistry.getKeys());
        else if (args.length == 3 || args.length == 4)
        {
            return getListOfStringsMatchingLastWord(args, "0");
        }

        return null;
    }
}
