/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.former;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.commands.parameters.*;
import ivorius.reccomplex.commands.parameters.CommandExpecting;
import ivorius.reccomplex.operation.OperationRegistry;
import ivorius.reccomplex.world.gen.feature.structure.OperationGenerateStructure;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectDuplicate extends CommandExpecting
{
    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "duplicate";
    }

    @Override
    public Expect<?> expect()
    {
        return RCExpect.expectRC()
                .pos("x", "y", "z")
                .named("rotation", "r").rotation()
                .flag("mirror", "m");
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args, expect()::declare);

        SelectionOwner selectionOwner = RCCommands.getSelectionOwner(commandSender, null, true);
        BlockArea area = selectionOwner.getSelection();

        BlockPos pos = parameters.pos("x", "y", "z", commandSender.getPosition(), false).require();
        AxisAlignedTransform2D transform = parameters.transform("rotation", "mirror").optional().orElse(AxisAlignedTransform2D.ORIGINAL);

        IvWorldData worldData = IvWorldData.capture(commandSender.getEntityWorld(), area, true);
        NBTTagCompound worldDataCompound = worldData.createTagCompound();

        GenericStructure structureInfo = GenericStructure.createDefaultStructure();
        structureInfo.worldDataCompound = worldDataCompound;

        OperationRegistry.queueOperation(new OperationGenerateStructure(structureInfo, null, transform, pos, true).prepare((WorldServer) commandSender.getEntityWorld()), commandSender);
    }
}
