/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockStates;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.ivtoolkit.world.MockWorld;
import ivorius.reccomplex.commands.parameters.RCParameter;
import net.minecraft.command.CommandException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import ivorius.ivtoolkit.math.IvShapeHelper;
import ivorius.reccomplex.RCConfig;
import net.minecraft.block.state.IBlockState;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectFillSphere extends CommandVirtual
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
            int[] dstMeta = args.length >= 2 ? RCParameter.parseMetadatas(args[1]) : new int[]{0};
            List<IBlockState> dst = IntStream.of(dstMeta).mapToObj(m -> BlockStates.fromMetadata(dstBlock, m)).collect(Collectors.toList());

            SelectionOwner selectionOwner = RCCommands.getSelectionOwner(commandSender, null, true);
            RCCommands.assertSize(commandSender, selectionOwner);
            BlockArea area = selectionOwner.getSelection();

            BlockPos p1 = area.getPoint1();
            BlockPos p2 = area.getPoint2();

            double[] spheroidOrigin = new double[]{(p1.getX() + p2.getX()) * 0.5, (p1.getY() + p2.getY()) * 0.5, (p1.getZ() + p2.getZ()) * 0.5};
            int[] areaSize = area.areaSize();
            double[] spheroidSize = new double[]{areaSize[0] * 0.5, areaSize[1] * 0.5, areaSize[2] * 0.5};

            for (BlockPos coord : area)
            {
                double[] coordPoint = new double[]{coord.getX(), coord.getY(), coord.getZ()};
                if (IvShapeHelper.isPointInSpheroid(coordPoint, spheroidOrigin, spheroidSize))
                {
                    IBlockState state = dst.get(world.rand().nextInt(dst.size()));
                    world.setBlockState(coord, state, 2);
                }
            }
        }
        else
        {
            throw ServerTranslations.wrongUsageException("commands.selectFillSphere.usage");
        }
    }
}
