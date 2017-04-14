/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.files.loading.LeveledRegistry;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.network.PacketSaveStructureHandler;
import ivorius.reccomplex.utils.RawResourceLocation;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.utils.algebra.ExpressionCache;
import ivorius.reccomplex.utils.expression.ResourceMatcher;
import ivorius.ivtoolkit.world.MockWorld;
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
import java.util.Arrays;
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
        if (args.length < 3)
            throw ServerTranslations.wrongUsageException("commands.rcmapall.usage");

        ResourceMatcher matcher = ExpressionCache.of(new ResourceMatcher(StructureRegistry.INSTANCE.ids()::contains), args[0]);
        ResourceDirectory directory = RCCommands.parseResourceDirectory(args[1]);

        ICommand other = server.getCommandManager().getCommands().get(args[2]);

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
                skipped ++;
                continue;
            }

            GenericStructure structure = (GenericStructure) info;

            IvWorldData worldData = structure.constructWorldData();
            MockWorld world = new MockWorld.WorldData(worldData);

            try
            {
                virtual.execute(world, new CommandSelecting.SelectingSender(commandSender, BlockPos.ORIGIN, worldData.blockCollection.area().getHigherCorner()),
                        Arrays.copyOfRange(args, 3, args.length));
            }
            catch (MockWorld.VirtualWorldException ex)
            {
                throw ServerTranslations.commandException("commands.rcmap.nonvirtual.arguments");
            }

            structure.worldDataCompound = worldData.createTagCompound();

            if (PacketSaveStructureHandler.write(commandSender, structure, id, directory, true, false))
                saved ++;
            else
                failed ++;
        }

        commandSender.sendMessage(ServerTranslations.format("commands.rcmapall.result", saved, directory, failed, skipped));

        ResourceDirectory.reload(RecurrentComplex.loader, LeveledRegistry.Level.CUSTOM);
        ResourceDirectory.reload(RecurrentComplex.loader, LeveledRegistry.Level.SERVER);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, StructureRegistry.INSTANCE.ids());
        else if (args.length == 2)
            return getListOfStringsMatchingLastWord(args, Arrays.asList(ResourceDirectory.values()));

        return super.getTabCompletions(server, sender, args, pos);
    }
}
