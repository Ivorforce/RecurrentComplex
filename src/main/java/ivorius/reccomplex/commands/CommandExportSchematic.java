/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockAreas;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.schematics.SchematicFile;
import ivorius.reccomplex.world.gen.feature.structure.schematics.SchematicLoader;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandExportSchematic extends CommandBase
{
    public static SchematicFile convert(IvWorldData worldData)
    {
        SchematicFile schematicFile = new SchematicFile((short) worldData.blockCollection.width, (short) worldData.blockCollection.height, (short) worldData.blockCollection.length);

        for (BlockPos coord : BlockAreas.mutablePositions(worldData.blockCollection.area()))
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
    public String getName()
    {
        return RCConfig.commandPrefix + "exportschematic";
    }

    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    @Override
    public String getUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.strucExportSchematic.usage");
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, StructureRegistry.INSTANCE.ids());

        return Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        SelectionOwner selectionOwner = RCCommands.getSelectionOwner(commandSender, null, true);
        BlockArea area = selectionOwner.getSelection();
        RCCommands.assertSize(commandSender, selectionOwner);

        String structureName;

        if (args.length >= 1)
            structureName = args[0];
        else
            structureName = "NewStructure_" + commandSender.getEntityWorld().rand.nextInt(1000);

        BlockPos lowerCoord = area.getLowerCorner();
        BlockPos higherCoord = area.getHigherCorner();

        IvWorldData data = IvWorldData.capture(commandSender.getEntityWorld(), new BlockArea(lowerCoord, higherCoord), true);
        SchematicFile schematicFile = convert(data);
        SchematicLoader.writeSchematicByName(schematicFile, structureName);

        commandSender.sendMessage(ServerTranslations.format("commands.strucExportSchematic.success", structureName));
    }
}
