/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockStates;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.MockWorld;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectFill extends VirtualCommand
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "fill";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.selectFill.usage");
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, Block.REGISTRY.getKeys());
        else if (args.length == 2)
            return getListOfStringsMatchingLastWord(args, "0");

        return Collections.emptyList();
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MockWorld world, ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length >= 1)
        {
            Block dstBlock = getBlockByText(commandSender, args[0]);
            int[] dstMeta = args.length >= 2 ? RCCommands.parseMetadatas(args[1]) : new int[]{0};
            List<IBlockState> dst = IntStream.of(dstMeta).mapToObj(m -> BlockStates.fromMetadata(dstBlock, m)).collect(Collectors.toList());

            SelectionOwner selectionOwner = RCCommands.getSelectionOwner(commandSender, null, true);
            RCCommands.assertSize(commandSender, selectionOwner);

            for (BlockPos coord : selectionOwner.getSelection())
            {
                IBlockState state = dst.get(world.rand().nextInt(dst.size()));
                world.setBlockState(coord, state, 2);
            }
        }
        else
        {
            throw ServerTranslations.wrongUsageException("commands.selectFill.usage");
        }
    }
}
