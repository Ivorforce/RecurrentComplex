/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.commands.parameters.Expect;
import ivorius.reccomplex.commands.parameters.Parameters;
import ivorius.reccomplex.operation.OperationRegistry;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.StructureGenerator;
import ivorius.reccomplex.world.gen.feature.structure.OperationGenerateStructure;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandImportStructure extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "import";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.strucImport.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        Parameters parameters = Parameters.of(this, args);

        String structureID = parameters.get().here().require();
        Structure<?> structure = parameters.get().structure().require();
        WorldServer world = parameters.get("d").dimension(commandSender).require();
        AxisAlignedTransform2D transform = RCCommands.transform(parameters.get("r"), parameters.get("m")).optional().orElse(AxisAlignedTransform2D.ORIGINAL);
        BlockPos pos = parameters.get("p").pos(commandSender, false).require();

        if (structure instanceof GenericStructure)
            OperationRegistry.queueOperation(new OperationGenerateStructure((GenericStructure) structure, structureID, transform, pos, true)
                    .withStructureID(structureID).prepare(world), commandSender);
        else
        {
            new StructureGenerator<>(structure).world(world)
                    .transform(transform).lowerCoord(pos).asSource(true).generate();
        }
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        return Expect.start()
                .next(StructureRegistry.INSTANCE.ids())
                .named("p")
                .next((server1, sender1, args1, pos1) -> getTabCompletionCoordinate(args1, 0, pos))
                .next((server1, sender1, args1, pos1) -> getTabCompletionCoordinate(args1, 1, pos))
                .next((server1, sender1, args1, pos1) -> getTabCompletionCoordinate(args1, 2, pos))
                .named("d")
                .next(RCCommands::completeDimension)
                .named("r")
                .next(RCCommands::completeRotation)
                .named("m")
                .next(RCCommands::completeMirror)
                .get(server, sender, args, pos);
    }
}
