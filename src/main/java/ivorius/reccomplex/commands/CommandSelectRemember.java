/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.utils.ServerTranslations;
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
public class CommandSelectRemember extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "remember";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.rcremember.usage");
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, Block.REGISTRY.getKeys());

        return Collections.emptyList();
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length < 1)
            throw ServerTranslations.wrongUsageException("commands.rcremember.usage");

        World world = commandSender.getEntityWorld();

        Block dstBlock = getBlockByText(commandSender, args[0]);
        int[] dstMeta = args.length >= 2 ? RCCommands.parseMetadatas(args[1]) : new int[]{0};
        List<IBlockState> dst = IntStream.of(dstMeta).mapToObj(dstBlock::getStateFromMeta).collect(Collectors.toList());

        for (BlockPos coord : RCCommands.getSelectionOwner(commandSender, null, true).getSelection())
        {
            IBlockState state = dst.get(world.rand.nextInt(dst.size()));
            world.setBlockState(coord, state, 3);
        }
    }
}
