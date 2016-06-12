/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockPositions;
import ivorius.ivtoolkit.tools.Mover;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.BlockPos;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.structures.schematics.SchematicFile;
import ivorius.reccomplex.structures.schematics.SchematicLoader;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandExportSchematic extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "exportschematic";
    }

    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.strucExportSchematic.usage");
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args) throws CommandException
    {
        EntityPlayerMP player = getCommandSenderAsPlayer(commandSender);

        BlockArea area;

//        if (args.length >= 6)
//        {
//            x = commandSender.getPlayerCoordinates().posX;
//            y = commandSender.getPlayerCoordinates().posY;
//            z = commandSender.getPlayerCoordinates().posZ;
//            x = MathHelper.floor_double(func_110666_a(commandSender, (double) x, args[0]));
//            y = MathHelper.floor_double(func_110666_a(commandSender, (double) y, args[1]));
//            z = MathHelper.floor_double(func_110666_a(commandSender, (double) z, args[2]));
//
//            width = Integer.valueOf(args[3]);
//            height = Integer.valueOf(args[4]);
//            length = Integer.valueOf(args[5]);
//        }
//        else
        {
            StructureEntityInfo structureEntityInfo = RCCommands.getStructureEntityInfo(player);

            if (structureEntityInfo.hasValidSelection())
            {
                area = new BlockArea(structureEntityInfo.selectedPoint1, structureEntityInfo.selectedPoint2);
            }
            else
            {
                throw ServerTranslations.commandException("commands.selectModify.noSelection");
            }
        }

        String structureName;

        if (args.length >= 1)
            structureName = args[0];
        else
            structureName = "NewStructure_" + commandSender.getEntityWorld().rand.nextInt(1000);

        BlockPos lowerCoord = area.getLowerCorner();
        BlockPos higherCoord = area.getHigherCorner();

        IvWorldData data = IvWorldData.capture(player.getEntityWorld(), new BlockArea(lowerCoord, higherCoord), true);
        SchematicFile schematicFile = convert(data, lowerCoord);
        SchematicLoader.writeSchematicByName(schematicFile, structureName);

        commandSender.addChatMessage(ServerTranslations.format("commands.strucExportSchematic.success", structureName));
    }

    public static SchematicFile convert(IvWorldData worldData, BlockPos referenceCoord)
    {
        SchematicFile schematicFile = new SchematicFile((short) worldData.blockCollection.width, (short) worldData.blockCollection.height, (short) worldData.blockCollection.length);
        BlockPos inverseReference = BlockPositions.invert(referenceCoord);

        for (BlockPos coord : worldData.blockCollection.area())
        {
            int index = schematicFile.getBlockIndex(coord);
            schematicFile.blockStates[index] = worldData.blockCollection.getBlockState(coord);
        }

        schematicFile.entityCompounds.clear();
        schematicFile.entityCompounds.addAll(worldData.entities);
        schematicFile.tileEntityCompounds.addAll(worldData.entities);
        schematicFile.tileEntityCompounds.addAll(worldData.tileEntities);

        return schematicFile;
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, StructureRegistry.INSTANCE.allStructureIDs());

        return null;
    }
}
