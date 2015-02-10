/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.worldgen.StructureSpawnContext;
import ivorius.reccomplex.worldgen.genericStructures.GenericStructureInfo;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectMove extends CommandSelectModify
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "move";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return "commands.selectMove.usage";
    }

    @Override
    public void processCommandSelection(EntityPlayerMP player, StructureEntityInfo structureEntityInfo, BlockCoord point1, BlockCoord point2, String[] args)
    {
        if (args.length < 3)
        {
            throw new WrongUsageException("commands.selectMove.usage");
        }

        int rotations = args.length >= 4 ? parseInt(player, args[3]) : 0;
        boolean mirrorX = args.length >= 5 && parseBoolean(player, args[4]);

        BlockArea area = new BlockArea(point1, point2);
        BlockCoord lowerCorner = area.getLowerCorner();

        int x = MathHelper.floor_double(func_110666_a(player, (double) lowerCorner.x, args[0]));
        int y = MathHelper.floor_double(func_110666_a(player, (double) lowerCorner.y, args[1]));
        int z = MathHelper.floor_double(func_110666_a(player, (double) lowerCorner.z, args[2]));

        IvWorldData worldData = new IvWorldData(player.worldObj, area, true);
        NBTTagCompound worldDataCompound = worldData.createTagCompound(area.getLowerCorner());

        World world = player.worldObj;

        for (BlockCoord coord : area)
            world.setBlockToAir(coord.x, coord.y, coord.z);

        GenericStructureInfo structureInfo = GenericStructureInfo.createDefaultStructure();
        structureInfo.worldDataCompound = worldDataCompound;

        structureInfo.generate(new StructureSpawnContext(world, world.rand, new BlockCoord(x, y, z), AxisAlignedTransform2D.transform(rotations, mirrorX), 0, true, structureInfo));

        structureEntityInfo.selectedPoint1 = point1.subtract(lowerCorner).add(x, y, z);
        structureEntityInfo.selectedPoint2 = point2.subtract(lowerCorner).add(x, y, z);
        structureEntityInfo.sendSelectionToClients(player);
    }
}
