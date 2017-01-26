/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.network.PacketEditStructureHandler;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructureInfo;
import ivorius.reccomplex.world.gen.feature.structure.schematics.SchematicFile;
import ivorius.reccomplex.world.gen.feature.structure.schematics.SchematicLoader;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandConvertSchematic extends CommandBase
{
    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "convertschematic";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.rcconvertschematic.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        args = RCCommands.parseQuotedWords(args);
        EntityPlayerMP player = getCommandSenderAsPlayer(commandSender);

        if (args.length < 1)
            throw ServerTranslations.wrongUsageException("commands.rcconvertschematic.usage");

        String schematicName = CommandImportSchematic.trimQuotes(args[0]);
        SchematicFile schematicFile = CommandImportSchematic.parseSchematic(schematicName);

        GenericStructureInfo structure = CommandExportStructure.getGenericStructure(commandSender, args.length >= 2 ? args[1] : null);

        structure.worldDataCompound = CommandExportSchematic.toWorldData(schematicFile).createTagCompound(null);

        PacketEditStructureHandler.openEditStructure(structure, schematicName, player);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        args = RCCommands.parseQuotedWords(args);

        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, SchematicLoader.currentSchematicFileNames()
            .stream().map(name -> name.contains(" ") ? String.format("\"%s\"", name) : name).collect(Collectors.toList()));
        else if (args.length == 2)
            return getListOfStringsMatchingLastWord(args, StructureRegistry.INSTANCE.ids());

        return Collections.emptyList();
    }
}
