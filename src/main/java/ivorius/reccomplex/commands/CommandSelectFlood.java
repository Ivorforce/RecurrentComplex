/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import ivorius.ivtoolkit.blocks.BlockStates;
import ivorius.ivtoolkit.world.MockWorld;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.commands.parameters.RCParameter;
import ivorius.reccomplex.utils.RCBlockLogic;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.utils.expression.PreloadedBooleanExpression;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectFlood extends CommandVirtual
{
    public static final int MAX_FLOOD = 50 * 50 * 50;

    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "flood";
    }

    @Override
    public String getUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.selectFlood.usage");
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, Block.REGISTRY.getKeys());
        else if (args.length == 2)
            return getListOfStringsMatchingLastWord(args, Collections.emptyList());

        List<String> ret = new ArrayList<>();

        ret.addAll(Arrays.stream(EnumFacing.values()).map(EnumFacing::getName2).collect(Collectors.toList()));
        ret.addAll(Arrays.stream(EnumFacing.Axis.values()).map(EnumFacing.Axis::getName).collect(Collectors.toList()));
        Collections.addAll(ret, "horizontal", "vertical");

        return getListOfStringsMatchingLastWord(args, ret);
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MockWorld world, ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length >= 2)
        {
            SelectionOwner selectionOwner = RCCommands.getSelectionOwner(commandSender, null, true);
            RCCommands.assertSize(commandSender, selectionOwner);

            PreloadedBooleanExpression<EnumFacing> facingExpression = PreloadedBooleanExpression.with(exp ->
            {
                exp.addConstants(EnumFacing.values());
                exp.addEvaluators(axis -> facing -> facing.getAxis() == axis, EnumFacing.Axis.values());
                exp.addEvaluator("horizontal", f -> f.getHorizontalIndex() >= 0);
                exp.addEvaluator("vertical", f -> f.getHorizontalIndex() < 0);
            });
            facingExpression.setExpression(buildString(args, 2));

            List<EnumFacing> available = Arrays.stream(EnumFacing.values()).filter(facingExpression).collect(Collectors.toList());

            List<BlockPos> dirty = Lists.newArrayList(selectionOwner.getSelection());
            Set<BlockPos> visited = Sets.newHashSet(dirty);

            Block dstBlock = getBlockByText(commandSender, args[0]);
            int[] dstMeta = args.length >= 2 ? RCParameter.parseMetadatas(args[1]) : new int[]{0};
            List<IBlockState> dst = IntStream.of(dstMeta).mapToObj(m -> BlockStates.fromMetadata(dstBlock, m)).collect(Collectors.toList());

            while (!dirty.isEmpty())
            {
                BlockPos pos = dirty.remove(dirty.size() - 1);

                for (EnumFacing facing : available)
                {
                    BlockPos offset = pos.offset(facing);
                    if (RCBlockLogic.isAir(world, offset) && visited.add(offset))
                        dirty.add(offset);
                }

                if (visited.size() > MAX_FLOOD)
                    throw new CommandException("Area too big to flood!");
            }

            for (BlockPos pos : visited)
            {
                IBlockState state = dst.get(world.rand().nextInt(dst.size()));
                world.setBlockState(pos, state, 2);
            }
        }
        else
        {
            throw ServerTranslations.wrongUsageException("commands.selectFlood.usage");
        }
    }
}
