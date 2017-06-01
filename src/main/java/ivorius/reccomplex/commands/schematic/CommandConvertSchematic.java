/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.schematic;

import ivorius.reccomplex.commands.structure.CommandExportStructure;
import ivorius.reccomplex.commands.parameters.RCExpect;
import ivorius.reccomplex.commands.parameters.RCParameters;
import ivorius.reccomplex.network.PacketEditStructureHandler;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import ivorius.reccomplex.world.gen.feature.structure.schematics.SchematicFile;
import ivorius.reccomplex.world.gen.feature.structure.schematics.SchematicLoader;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandConvertSchematic extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "convert";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.rcconvertschematic.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args);

        EntityPlayerMP player = getCommandSenderAsPlayer(commandSender);

        if (args.length < 1)
            throw ServerTranslations.wrongUsageException("commands.rcconvertschematic.usage");

        String schematicName = parameters.get().first().require();
        SchematicFile schematicFile = CommandImportSchematic.parseSchematic(schematicName);

        GenericStructure structure = CommandExportStructure.getNewGenericStructure(commandSender, parameters.rc("from"));

        structure.worldDataCompound = CommandExportSchematic.toWorldData(schematicFile).createTagCompound();

        PacketEditStructureHandler.openEditStructure(structure, schematicName, player);
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        return RCExpect.startRC()
                .next(SchematicLoader.currentSchematicFileNames()
                        .stream().map(name -> name.contains(" ") ? String.format("\"%s\"", name) : name).collect(Collectors.toList()))
                .named("from").structure()
                .get(server, sender, args, pos);
    }
}
