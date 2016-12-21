/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockStates;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.utils.expression.PositionedBlockMatcher;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectReplace extends CommandBase
{
    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "replace";
    }

    @Override
    public String getUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.selectReplace.usage");
    }

    @Nonnull
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, Block.REGISTRY.getKeys());
        else if (args.length == 2)
            return getListOfStringsMatchingLastWord(args, IntStream.range(0, 16).mapToObj(String::valueOf).collect(Collectors.toList()));
        else
            return getListOfStringsMatchingLastWord(args, Block.REGISTRY.getKeys());
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length >= 3)
        {
            World world = commandSender.getEntityWorld();

            String src = buildString(args, 2);

            Block dstBlock = getBlockByText(commandSender, args[0]);
            int[] dstMeta = RCCommands.parseMetadatas(args[1]);
            List<IBlockState> dst = IntStream.of(dstMeta).mapToObj(m -> BlockStates.fromMetadata(dstBlock, m)).collect(Collectors.toList());

            PositionedBlockMatcher matcher = new PositionedBlockMatcher(RecurrentComplex.specialRegistry, src);

            SelectionOwner selectionOwner = RCCommands.getSelectionOwner(commandSender, null, true);
            RCCommands.assertSize(commandSender, selectionOwner);

            for (BlockPos coord : selectionOwner.getSelection())
            {
                if (matcher.test(PositionedBlockMatcher.Argument.at(world, coord)))
                {
                    IBlockState state = dst.get(world.rand.nextInt(dst.size()));
                    world.setBlockState(coord, state, 3);
                }
            }
        }
        else
        {
            throw ServerTranslations.wrongUsageException("commands.selectReplace.usage");
        }
    }
}
