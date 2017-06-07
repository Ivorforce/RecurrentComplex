/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.structure;

import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.ivtoolkit.world.MockWorld;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.commands.CommandSelecting;
import ivorius.reccomplex.commands.CommandVirtual;
import ivorius.reccomplex.commands.parameters.CommandExpecting;
import ivorius.reccomplex.commands.parameters.Expect;
import ivorius.reccomplex.commands.parameters.RCExpect;
import ivorius.reccomplex.commands.parameters.RCParameters;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.network.PacketSaveStructureHandler;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandMapStructure extends CommandExpecting
{
    @Nonnull
    public static MapResult map(String structureID, ResourceDirectory directory, ICommandSender commandSender, CommandVirtual command, String[] args, boolean inform) throws CommandException
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
                .structure()
                .virtualCommand()
                .commandArguments(p -> p.get(1)).repeat()
                .named("directory", "d").resourceDirectory();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args, expect()::declare);

        String id = parameters.get().first().require();
        ResourceDirectory directory = parameters.get("directory").resourceDirectory().optional().orElse(ResourceDirectory.ACTIVE);
        CommandVirtual virtual = parameters.get(1).virtualCommand(server).require();
        String[] virtualArgs = parameters.get(2).varargs();

        map(id, directory, sender, virtual, virtualArgs, true);
    }

    public enum MapResult
    {
        SUCCESS, FAILED, SKIPPED
    }
}
