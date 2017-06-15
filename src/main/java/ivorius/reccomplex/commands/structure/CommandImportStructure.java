/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.structure;

import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.mcopts.commands.CommandExpecting;
import ivorius.mcopts.commands.parameters.*;
import ivorius.mcopts.commands.parameters.expect.Expect;
import ivorius.mcopts.commands.parameters.expect.MCE;
import ivorius.reccomplex.commands.parameters.IvP;
import ivorius.reccomplex.commands.parameters.expect.RCE;
import ivorius.reccomplex.commands.parameters.RCP;
import ivorius.reccomplex.operation.OperationRegistry;
import ivorius.reccomplex.utils.RCBlockAreas;
import ivorius.reccomplex.world.gen.feature.StructureGenerator;
import ivorius.reccomplex.world.gen.feature.structure.OperationGenerateStructure;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.StructureBoundingBox;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandImportStructure extends CommandExpecting
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
    public void expect(Expect expect)
    {
        expect.then(RCE::structure).then(MCE.pos("x", "y", "z"))
                .named("dimension", "d").then(MCE::dimension)
                .named("rotation", "r").then(MCE::rotation)
                .flag("mirror", "m")
                .flag("select", "s");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        Parameters parameters = Parameters.of(args, expect()::declare);

        String structureID = parameters.get(0).require();
        Structure<?> structure = parameters.get(0).to(RCP::structure).require();
        WorldServer world = parameters.get("dimension").to(MCP.dimension(server, sender)).require();
        AxisAlignedTransform2D transform = parameters.get(IvP.transform("rotation", "mirror")).optional().orElse(AxisAlignedTransform2D.ORIGINAL);
        BlockPos pos = parameters.get(MCP.pos("x", "y", "z", sender.getPosition(), false)).require();
        boolean select = parameters.has("select");

        StructureGenerator<?> generator = new StructureGenerator<>(structure).world(world)
                .transform(transform).lowerCoord(pos).asSource(true);

        // Can never not place so don't handle
        //noinspection OptionalGetWithoutIsPresent
        StructureBoundingBox boundingBox = generator.boundingBox().get();

        if (structure instanceof GenericStructure && world == sender.getEntityWorld())
            OperationRegistry.queueOperation(new OperationGenerateStructure((GenericStructure) structure, structureID, transform, pos, true)
                    .withStructureID(structureID).prepare(world), sender);
        else
            generator.generate();

        if (select)
        {
            SelectionOwner owner = RCCommands.getSelectionOwner(sender, null, false);
            owner.setSelection(RCBlockAreas.from(boundingBox));
        }
    }
}
