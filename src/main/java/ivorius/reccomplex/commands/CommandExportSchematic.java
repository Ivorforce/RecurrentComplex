/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.network.PacketEditStructureHandler;
import ivorius.reccomplex.schematics.SchematicFile;
import ivorius.reccomplex.schematics.SchematicLoader;
import ivorius.reccomplex.worldgen.StructureHandler;
import ivorius.reccomplex.worldgen.StructureInfo;
import ivorius.reccomplex.worldgen.genericStructures.GenericStructureInfo;
import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;

import java.util.List;
import java.util.Set;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandExportSchematic extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "strucExportSchematic";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return "commands.strucExportSchematic.usage";
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args)
    {
        EntityPlayerMP player = getCommandSenderAsPlayer(commandSender);

        if (player == null)
        {
            throw new WrongUsageException("commands.strucExport.noPlayer");
        }

        int x, y, z;
        int width, height, length;

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
            StructureEntityInfo structureEntityInfo = StructureEntityInfo.getStructureEntityInfo(player);

            if (structureEntityInfo.hasValidSelection())
            {
                BlockCoord smaller = structureEntityInfo.selectedPoint1.getLowerCorner(structureEntityInfo.selectedPoint2);
                BlockCoord bigger = structureEntityInfo.selectedPoint1.getHigherCorner(structureEntityInfo.selectedPoint2);
                x = smaller.x;
                y = smaller.y;
                z = smaller.z;
                width = bigger.x - smaller.x + 1;
                height = bigger.y - smaller.y + 1;
                length = bigger.z - smaller.z + 1;
            }
            else
            {
                throw new WrongUsageException("commands.selectModify.noSelection");
            }
        }

        String structureName;

        if (args.length >= 1)
            structureName = args[0];
        else
            structureName = "NewStructure_" + commandSender.getEntityWorld().rand.nextInt(1000);

        BlockCoord lowerCoord = new BlockCoord(x, y, z);
        BlockCoord higherCoord = new BlockCoord(x + width - 1, y + height - 1, z + length - 1);

        IvWorldData data = new IvWorldData(player.getEntityWorld(), new BlockArea(lowerCoord, higherCoord), true);
        SchematicFile schematicFile = convert(data, lowerCoord);
        SchematicLoader.writeSchematicByName(schematicFile, structureName);

        commandSender.addChatMessage(new ChatComponentTranslation("commands.strucExportSchematic.success", structureName));
    }

    public static SchematicFile convert(IvWorldData worldData, BlockCoord referenceCoord)
    {
        SchematicFile schematicFile = new SchematicFile((short) worldData.blockCollection.width, (short) worldData.blockCollection.height, (short) worldData.blockCollection.length);

        for (BlockCoord coord : worldData.blockCollection)
        {
            int index = schematicFile.getBlockIndex(coord.x, coord.y, coord.z);
            schematicFile.blocks[index] = worldData.blockCollection.getBlock(coord);
            schematicFile.metadatas[index] = worldData.blockCollection.getMetadata(coord);
        }

        for (TileEntity tileEntity : worldData.tileEntities)
        {
            NBTTagCompound teCompound = new NBTTagCompound();

            IvWorldData.moveTileEntityForGeneration(tileEntity, referenceCoord.invert());
            tileEntity.writeToNBT(teCompound);
            IvWorldData.moveTileEntityForGeneration(tileEntity, referenceCoord);

            schematicFile.tileEntityCompounds.add(teCompound);
        }

        for (Entity entity : worldData.entities)
        {
            NBTTagCompound entityCompound = new NBTTagCompound();

            IvWorldData.moveEntityForGeneration(entity, referenceCoord.invert());
            entity.writeToNBTOptional(entityCompound);
            IvWorldData.moveEntityForGeneration(entity, referenceCoord);

            schematicFile.entityCompounds.add(entityCompound);
        }

        return schematicFile;
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args)
    {
        if (args.length == 1)
        {
            Set<String> allStructureNames = StructureHandler.getAllStructureNames();

            return getListOfStringsMatchingLastWord(args, allStructureNames.toArray(new String[allStructureNames.size()]));
        }

        return null;
    }

    public static GenericStructureInfo getGenericStructureInfo(String name)
    {
        StructureInfo structureInfo = StructureHandler.getStructure(name);

        if (structureInfo == null)
        {
            throw new WrongUsageException("commands.structure.notRegistered", name);
        }

        GenericStructureInfo genericStructureInfo = structureInfo.copyAsGenericStructureInfo();

        if (genericStructureInfo == null)
        {
            throw new WrongUsageException("commands.structure.notGeneric", name);
        }

        return genericStructureInfo;
    }
}
