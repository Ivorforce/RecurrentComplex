/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.structure;

import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.ivtoolkit.world.MockWorld;
import ivorius.mcopts.commands.CommandExpecting;
import ivorius.mcopts.commands.DelegatingSender;
import ivorius.mcopts.commands.parameters.NaP;
import ivorius.mcopts.commands.parameters.Parameter;
import ivorius.mcopts.commands.parameters.Parameters;
import ivorius.mcopts.commands.parameters.expect.Expect;
import ivorius.mcopts.commands.parameters.expect.MCE;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.capability.CapabilitySelection;
import ivorius.reccomplex.commands.CommandVirtual;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.commands.RCTextStyle;
import ivorius.reccomplex.commands.parameters.RCP;
import ivorius.reccomplex.commands.parameters.expect.RCE;
import ivorius.reccomplex.files.loading.LeveledRegistry;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.network.PacketSaveStructureHandler;
import ivorius.reccomplex.utils.RawResourceLocation;
import ivorius.reccomplex.utils.expression.ResourceExpression;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;

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
        GenericStructure structure = Parameter.makeUp(null, 0, structureID)
                .to(RCP::structure).require().copyAsGenericStructure();

        if (structure == null)
        {
            if (inform)
                throw RecurrentComplex.translations.commandException("commands.structure.notGeneric", structureID);

            return MapResult.SKIPPED;
        }

        IvWorldData worldData = structure.constructWorldData();
        MockWorld world = new MockWorld.WorldData(worldData);

        try
        {
            command.execute(world, new SelectingSender(commandSender, BlockPos.ORIGIN, worldData.blockCollection.area().getHigherCorner()),
                    args);
        }
        catch (MockWorld.VirtualWorldException ex)
        {
            throw RecurrentComplex.translations.commandException("commands.rcmap.nonvirtual.arguments");
        }

        structure.worldDataCompound = worldData.createTagCompound();

        if (directory == null)
            return MapResult.SUCCESS;

        return PacketSaveStructureHandler.write(commandSender, structure, structureID, directory, true, inform)
                ? MapResult.SUCCESS
                : MapResult.FAILED;
    }

    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "map";
    }

    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    @Override
    public void expect(Expect expect)
    {
        expect
                .then(RCE::structure).descriptionU("resource expression|structure").required()
                .then(RCE::virtualCommand)
                .stopInterpreting()
                .then(MCE::commandArguments, p -> p.get(1)).repeat()
                .named("directory", "d").then(RCE::resourceDirectory)
                .flag("nosave", "n")
        ;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        Parameters parameters = Parameters.of(args, expect()::declare);

        ResourceExpression expression = parameters.get(0).to(RCP::expression, new ResourceExpression(StructureRegistry.INSTANCE::has)).require();

        CommandVirtual virtual = parameters.get(1).to(RCP::virtualCommand, server).require();
        String[] virtualArgs = parameters.get(2).to(NaP::varargs).require();

        ResourceDirectory directory = parameters.has("nosave") ? null :
                parameters.get("directory").to(RCP::resourceDirectory).optional().orElse(ResourceDirectory.ACTIVE);

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
            commandSender.sendMessage(RecurrentComplex.translations.format("commands.rcmapall.result", saved, RCTextStyle.path(directory), failed, skipped));

        RCCommands.tryReload(RecurrentComplex.loader, LeveledRegistry.Level.CUSTOM);
        RCCommands.tryReload(RecurrentComplex.loader, LeveledRegistry.Level.SERVER);
    }

    public enum MapResult
    {
        SUCCESS, FAILED, SKIPPED
    }

    public static class SelectingSender extends DelegatingSender
    {
        public CapabilitySelection capabilitySelection;

        public SelectingSender(ICommandSender sender, BlockPos point1, BlockPos point2)
        {
            super(sender);
            capabilitySelection = new CapabilitySelection(point1, point2);
        }

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
        {
            if (capability == CapabilitySelection.CAPABILITY)
                return true;
            return super.hasCapability(capability, facing);
        }

        @Nullable
        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
        {
            if (capability == CapabilitySelection.CAPABILITY)
                return (T) capabilitySelection;
            return super.getCapability(capability, facing);
        }
    }
}
