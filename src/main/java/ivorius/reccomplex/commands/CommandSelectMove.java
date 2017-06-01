/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.commands.parameters.RCExpect;
import ivorius.reccomplex.commands.parameters.RCParameters;
import ivorius.reccomplex.operation.OperationRegistry;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.structure.OperationClearArea;
import ivorius.reccomplex.world.gen.feature.structure.OperationGenerateStructure;
import ivorius.reccomplex.world.gen.feature.structure.OperationMulti;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectMove extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "move";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.selectMove.usage");
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args, "mirror");

        BlockPos pos = parameters.pos("x", "y", "z", commandSender.getPosition(), false).require();
        AxisAlignedTransform2D transform = parameters.transform("rotation", "mirror").optional().orElse(AxisAlignedTransform2D.ORIGINAL);

        SelectionOwner selectionOwner = RCCommands.getSelectionOwner(commandSender, null, true);
        RCCommands.assertSize(commandSender, selectionOwner);
        BlockArea area = selectionOwner.getSelection();

        IvWorldData worldData = IvWorldData.capture(commandSender.getEntityWorld(), area, true);
        NBTTagCompound worldDataCompound = worldData.createTagCompound();

        GenericStructure structureInfo = GenericStructure.createDefaultStructure();
        structureInfo.worldDataCompound = worldDataCompound;

        OperationRegistry.queueOperation(new OperationMulti(new OperationClearArea(area), new OperationGenerateStructure(structureInfo, null, transform, pos, true).prepare((WorldServer) commandSender.getEntityWorld())), commandSender);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        return RCExpect.startRC()
                .pos("x", "y", "z")
                .named("rotation").rotation()
                .flag("mirror")
                .get(server, sender, args, pos);
    }
}
