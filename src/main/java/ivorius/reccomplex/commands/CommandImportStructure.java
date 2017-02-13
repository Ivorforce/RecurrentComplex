/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.world.gen.feature.structure.*;
import ivorius.reccomplex.world.gen.feature.StructureGenerator;
import net.minecraft.command.CommandException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.operation.OperationRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructureInfo;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.Collections;
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
        WorldServer world = (WorldServer) commandSender.getEntityWorld();

        if (args.length <= 0)
            throw ServerTranslations.wrongUsageException("commands.strucImport.usage");

        String structureID = args[0];
        StructureInfo<?> structureInfo = RCCommands.getGenericStructure(structureID);

        BlockPos coord = RCCommands.tryParseBlockPos(commandSender, args, 1, false);

        AxisAlignedTransform2D transform = RCCommands.tryParseTransform(args, 4);

        if (structureInfo instanceof GenericStructureInfo)
            OperationRegistry.queueOperation(new OperationGenerateStructure((GenericStructureInfo) structureInfo, structureID, transform, coord, true).withStructureID(structureID).prepare(world), commandSender);
        else
        {
            new StructureGenerator<>(structureInfo).world(world)
                    .transform(transform).lowerCoord(coord).asSource(true).generate();
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, StructureRegistry.INSTANCE.ids());
        }
        else if (args.length == 2 || args.length == 3 || args.length == 4)
            return getTabCompletionCoordinate(args, args.length - 1, pos);
        else if (args.length == 5 || args.length == 6)
            return RCCommands.completeTransform(args, args.length - 5);

        return Collections.emptyList();
    }
}
