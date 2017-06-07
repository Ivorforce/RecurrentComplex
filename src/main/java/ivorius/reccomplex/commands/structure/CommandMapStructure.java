/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.structure;

import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.ivtoolkit.world.MockWorld;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.commands.CommandSelecting;
import ivorius.reccomplex.commands.CommandVirtual;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.commands.RCTextStyle;
import ivorius.reccomplex.commands.parameters.*;
import ivorius.reccomplex.files.loading.LeveledRegistry;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.network.PacketSaveStructureHandler;
import ivorius.reccomplex.utils.RawResourceLocation;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.utils.expression.ResourceExpression;
import ivorius.reccomplex.utils.optional.IvOptional;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandMapStructure extends CommandExpecting
{
    @Nonnull
    public static MapResult map(String structureID, @Nullable ResourceDirectory directory, ICommandSender commandSender, CommandVirtual command, String[] args, boolean inform) throws CommandException
    {
        Structure<?> info = StructureRegistry.INSTANCE.get(structureID);

        if (!(info instanceof GenericStructure))
        {
            if (inform)
                throw ServerTranslations.commandException("commands.structure.notGeneric", structureID);

            return MapResult.SKIPPED;
        }

        GenericStructure structure = (GenericStructure) info;

        IvWorldData worldData = structure.constructWorldData();
        MockWorld world = new MockWorld.WorldData(worldData);

        try
        {
            command.execute(world, new CommandSelecting.SelectingSender(commandSender, BlockPos.ORIGIN, worldData.blockCollection.area().getHigherCorner()),
                    args);
        }
        catch (MockWorld.VirtualWorldException ex)
        {
            throw ServerTranslations.commandException("commands.rcmap.nonvirtual.arguments");
        }

        structure.worldDataCompound = worldData.createTagCompound();

        if (directory == null)
            return MapResult.SUCCESS;

        return PacketSaveStructureHandler.write(commandSender, structure, structureID, directory, true, inform)
                ? MapResult.SUCCESS
                : MapResult.FAILED;
    }

    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "map";
    }

    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    @Override
    public Expect<?> expect()
    {
        return RCExpect.expectRC()
                .structure().descriptionU("resource expression|structure").required()
                .virtualCommand()
                .stopNamed()
                .commandArguments(p -> p.get(1)).repeat()
                .named("directory", "d").resourceDirectory()
                .flag("nosave", "n")
                ;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args, expect()::declare);

        ResourceExpression expression = parameters.get(0).expression(new ResourceExpression(StructureRegistry.INSTANCE::has)).require();

        CommandVirtual virtual = parameters.get(1).virtualCommand(server).require();
        String[] virtualArgs = parameters.get(2).varargs().require();

        ResourceDirectory directory = parameters.has("nosave") ? null :
                parameters.get("directory").resourceDirectory().optional().orElse(ResourceDirectory.ACTIVE);

        List<String> relevant = StructureRegistry.INSTANCE.ids().stream()
                .filter(id -> expression.test(new RawResourceLocation(StructureRegistry.INSTANCE.status(id).getDomain(), id)))
                .collect(Collectors.toList());

        boolean inform = relevant.size() == 1;

        int saved = 0, failed = 0, skipped = 0;
        for (String id : relevant)
        {
            switch (map(id, directory, commandSender, virtual, virtualArgs, inform))
            {
                case SKIPPED:
                    skipped++;
                    break;
                case SUCCESS:
                    saved++;
                    break;
                default:
                    failed++;
                    break;
            }
        }

        if (!inform)
            commandSender.sendMessage(ServerTranslations.format("commands.rcmapall.result", saved, RCTextStyle.path(directory), failed, skipped));

        RCCommands.tryReload(RecurrentComplex.loader, LeveledRegistry.Level.CUSTOM);
        RCCommands.tryReload(RecurrentComplex.loader, LeveledRegistry.Level.SERVER);
    }

    public enum MapResult
    {
        SUCCESS, FAILED, SKIPPED
    }
}
