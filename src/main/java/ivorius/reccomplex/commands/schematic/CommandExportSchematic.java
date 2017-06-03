/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.schematic;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockAreas;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.commands.parameters.RCExpect;
import ivorius.reccomplex.commands.parameters.RCParameters;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.structure.schematics.SchematicFile;
import ivorius.reccomplex.world.gen.feature.structure.schematics.SchematicLoader;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandExportSchematic extends CommandBase
{
    public static SchematicFile toSchematic(IvWorldData worldData)
    {
        SchematicFile schematicFile = new SchematicFile((short) worldData.blockCollection.width, (short) worldData.blockCollection.height, (short) worldData.blockCollection.length);

        for (BlockPos pos : BlockAreas.mutablePositions(worldData.blockCollection.area()))
            schematicFile.setBlockState(pos, worldData.blockCollection.getBlockState(pos));

        schematicFile.entityCompounds.addAll(worldData.entities);
        schematicFile.tileEntityCompounds.addAll(worldData.tileEntities);

        return schematicFile;
    }

    public static IvWorldData toWorldData(SchematicFile schematicFile)
    {
        IvWorldData worldData = new IvWorldData(new IvBlockCollection(schematicFile.width, schematicFile.height, schematicFile.length),
                schematicFile.tileEntityCompounds, schematicFile.entityCompounds);

        for (BlockPos pos : schematicFile.area())
            worldData.blockCollection.setBlockState(pos, schematicFile.getBlockState(pos));

        return worldData;
    }

    @Override
    public String getName()
    {
        return "export";
    }

    @Override
    public String getUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.rcexportschematic.usage");
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        return RCExpect.expectRC()
                .schematic()
                .get(server, sender, args, pos);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args);

        SelectionOwner selectionOwner = RCCommands.getSelectionOwner(commandSender, null, true);
        BlockArea area = selectionOwner.getSelection();
        RCCommands.assertSize(commandSender, selectionOwner);

        String structureName = parameters.get().first().optional()
                .orElse("NewStructure_" + commandSender.getEntityWorld().rand.nextInt(1000));

        BlockPos lowerCoord = area.getLowerCorner();
        BlockPos higherCoord = area.getHigherCorner();

        IvWorldData data = IvWorldData.capture(commandSender.getEntityWorld(), new BlockArea(lowerCoord, higherCoord), true);
        SchematicFile schematicFile = toSchematic(data);
        SchematicLoader.writeSchematicByName(schematicFile, structureName);

        commandSender.sendMessage(ServerTranslations.format("commands.strucExportSchematic.success", structureName));
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 4;
    }
}
