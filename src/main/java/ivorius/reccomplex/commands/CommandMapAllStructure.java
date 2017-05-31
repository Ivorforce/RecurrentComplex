/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.ivtoolkit.world.MockWorld;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.commands.parameters.RCExpect;
import ivorius.reccomplex.commands.parameters.RCParameters;
import ivorius.reccomplex.files.loading.LeveledRegistry;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.network.PacketSaveStructureHandler;
import ivorius.reccomplex.utils.RawResourceLocation;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.utils.expression.ResourceMatcher;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandMapAllStructure extends CommandBase
{
    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "mapall";
    }

    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    @Override
    public String getUsage(ICommandSender commandSender)
    {
        return ServerTranslations.usage("commands.rcmapall.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args);

        ResourceMatcher matcher = RCCommands.resourceMatcher(parameters.get("exp"), StructureRegistry.INSTANCE::has).require();
        ResourceDirectory directory = parameters.rc("dir").resourceDirectory().optional().orElse(ResourceDirectory.ACTIVE);

        ICommand other = server.getCommandManager().getCommands().get(parameters.get().at(1).require());

        if (!(other instanceof CommandVirtual))
            throw ServerTranslations.commandException("commands.rcmap.nonvirtual");

        CommandVirtual virtual = (CommandVirtual) other;

        int saved = 0, failed = 0, skipped = 0;
        for (String id : StructureRegistry.INSTANCE.ids())
        {
            if (!matcher.test(new RawResourceLocation(StructureRegistry.INSTANCE.status(id).getDomain(), id)))
                continue;

            Structure<?> info = StructureRegistry.INSTANCE.get(id);

            if (!(info instanceof GenericStructure))
            {
                skipped++;
                continue;
            }

            GenericStructure structure = (GenericStructure) info;

            IvWorldData worldData = structure.constructWorldData();
            MockWorld world = new MockWorld.WorldData(worldData);

            try
            {
                virtual.execute(world, new CommandSelecting.SelectingSender(commandSender, BlockPos.ORIGIN, worldData.blockCollection.area().getHigherCorner()),
                        parameters.get().move(2).varargs());
            }
            catch (MockWorld.VirtualWorldException ex)
            {
                throw ServerTranslations.commandException("commands.rcmap.nonvirtual.arguments");
            }

            structure.worldDataCompound = worldData.createTagCompound();

            if (PacketSaveStructureHandler.write(commandSender, structure, id, directory, true, false))
                saved++;
            else
                failed++;
        }

        commandSender.sendMessage(ServerTranslations.format("commands.rcmapall.result", saved, directory, failed, skipped));

        RCCommands.tryReload(RecurrentComplex.loader, LeveledRegistry.Level.CUSTOM);
        RCCommands.tryReload(RecurrentComplex.loader, LeveledRegistry.Level.SERVER);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        return RCExpect.startRC()
                .any("\"\"")
                .skip(1)
                .skip(1).repeat()
                .named("exp").structure()
                .named("dir").resourceDirectory()
                .get(server, sender, args, pos);
    }
}
