/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.commands.parameters.RCExpect;
import ivorius.reccomplex.commands.parameters.RCParameters;
import ivorius.reccomplex.operation.OperationRegistry;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.StructureGenerator;
import ivorius.reccomplex.world.gen.feature.structure.OperationGenerateStructure;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
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
    public String getName()
    {
        return RCConfig.commandPrefix + "import";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.strucImport.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args);

        String structureID = parameters.get().first().require();
        Structure<?> structure = parameters.rc().structure().require();
        WorldServer world = parameters.mc("d").dimension(commandSender).require();
        AxisAlignedTransform2D transform = parameters.iv("r").transform(parameters.has("m")).optional().orElse(AxisAlignedTransform2D.ORIGINAL);
        BlockPos pos = parameters.mc("p").pos(commandSender.getPosition(), false).require();

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
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        return RCExpect.startRC()
                .structure()
                .named("p").pos()
                .named("d").dimension()
                .named("r").rotation()
                .flag("m")
                .get(server, sender, args, pos);
    }
}
