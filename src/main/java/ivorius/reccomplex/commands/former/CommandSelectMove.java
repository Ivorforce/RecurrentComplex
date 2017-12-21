/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.former;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.mcopts.commands.CommandExpecting;
import ivorius.mcopts.commands.parameters.MCP;
import ivorius.mcopts.commands.parameters.NaP;
import ivorius.mcopts.commands.parameters.Parameters;
import ivorius.mcopts.commands.parameters.expect.Expect;
import ivorius.mcopts.commands.parameters.expect.MCE;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.commands.parameters.IvP;
import ivorius.reccomplex.commands.parameters.RCP;
import ivorius.reccomplex.operation.OperationClearArea;
import ivorius.reccomplex.operation.OperationGenerateStructure;
import ivorius.reccomplex.operation.OperationMulti;
import ivorius.reccomplex.operation.OperationRegistry;
import ivorius.reccomplex.utils.RCBlockAreas;
import ivorius.reccomplex.world.gen.feature.StructureGenerator;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.StructureBoundingBox;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectMove extends CommandExpecting
{
    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "move";
    }

    @Override
    public void expect(Expect expect)
    {
        expect.then(MCE.pos("x", "y", "z"))
                .named("rotation", "r").then(MCE::rotation)
                .flag("mirror", "m")
                .flag("noselect", "s")
                .flag("duplicate", "c")
                .named("times", "t").any(1, 2, 3)
        ;
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        Parameters parameters = Parameters.of(args, expect()::declare);

        SelectionOwner selectionOwner = RCCommands.getSelectionOwner(commandSender, null, true);
        RCCommands.assertSize(commandSender, selectionOwner);

        BlockPos move = parameters.get(RCP.pos("x", "y", "z", BlockPos.ORIGIN, false)).require();
        AxisAlignedTransform2D transform = parameters.get(IvP.transform("rotation", "mirror")).optional().orElse(AxisAlignedTransform2D.ORIGINAL);
        boolean noselect = parameters.has("noselect");
        boolean duplicate = parameters.has("duplicate");
        int times = parameters.get("times").to(NaP::asInt).optional().orElse(1);

        BlockArea area = selectionOwner.getSelection();

        IvWorldData worldData = IvWorldData.capture(commandSender.getEntityWorld(), area, true);
        NBTTagCompound worldDataCompound = worldData.createTagCompound();

        GenericStructure structure = GenericStructure.createDefaultStructure();
        structure.worldDataCompound = worldDataCompound;

        BlockPos pos = selectionOwner.getSelection().getLowerCorner();
        for (int i = 0; i < times; i++)
        {
            pos = pos.add(move);

            if (duplicate)
                OperationRegistry.queueOperation(new OperationGenerateStructure(structure, null, transform, pos, true).prepare((WorldServer) commandSender.getEntityWorld()), commandSender);
            else
                OperationRegistry.queueOperation(new OperationMulti(new OperationClearArea(area), new OperationGenerateStructure(structure, null, transform, pos, true).prepare((WorldServer) commandSender.getEntityWorld())), commandSender);
        }

        if (!noselect)
        {
            StructureGenerator<GenericStructure.InstanceData> generator = new StructureGenerator<>(structure)
                    .transform(transform).lowerCoord(pos);
            //noinspection OptionalGetWithoutIsPresent
            StructureBoundingBox boundingBox = generator.boundingBox().get();
            selectionOwner.setSelection(RCBlockAreas.from(boundingBox));
        }
    }
}
