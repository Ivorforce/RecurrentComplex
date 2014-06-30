/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.IvShapeHelper;
import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

import java.util.List;
import java.util.Set;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectFillSphere extends CommandSelectModify
{
    @Override
    public String getCommandName()
    {
        return "selectFillSphere";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return "commands.selectFillSphere.usage";
    }

    @Override
    public void processCommandSelection(EntityPlayerMP player, StructureEntityInfo structureEntityInfo, BlockCoord point1, BlockCoord point2, String[] args)
    {
        if (args.length >= 1)
        {
            World world = player.getEntityWorld();

            Block dst = getBlock(args[0]);
            int[] dstMeta = args.length >= 2 ? getMetadatas(args[1]) : new int[]{0};

            BlockArea area = new BlockArea(point1, point2);

            double[] spheroidOrigin = new double[]{(point1.x + point2.x) * 0.5, (point1.y + point2.y) * 0.5, (point1.z + point2.z) * 0.5};
            int[] areaSize = area.areaSize();
            double[] spheroidSize = new double[]{areaSize[0] * 0.5, areaSize[1] * 0.5, areaSize[2] * 0.5};

            for (BlockCoord coord : area)
            {
                double[] coordPoint = new double[]{coord.x, coord.y, coord.z};
                if (IvShapeHelper.isPointInSpheroid(coordPoint, spheroidOrigin, spheroidSize))
                {
                    world.setBlock(coord.x, coord.y, coord.z, dst, dstMeta[player.getRNG().nextInt(dstMeta.length)], 3);
                }
            }
        }
        else
        {
            throw new WrongUsageException("commands.selectFillSphere.usage");
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args)
    {
        if (args.length == 1)
        {
            Set<String> allStructureNames = Block.blockRegistry.getKeys();

            return getListOfStringsMatchingLastWord(args, allStructureNames.toArray(new String[allStructureNames.size()]));
        }
        else if (args.length == 2)
        {
            return getListOfStringsMatchingLastWord(args, "0");
        }

        return null;
    }
}
