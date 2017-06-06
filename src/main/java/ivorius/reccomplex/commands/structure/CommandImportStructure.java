/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.structure;

import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.commands.parameters.*;
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
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "import";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public Expect<?> expect()
    {
        return RCExpect.expectRC()
                .structure()
                .pos("x", "y", "z")
                .named("dimension", "d").dimension()
                .named("rotation", "r").rotation()
                .flag("mirror", "m")
                .flag("select", "s");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args, expect()::declare);

        String structureID = parameters.get().first().require();
        Structure<?> structure = parameters.get().structure().require();
        WorldServer world = parameters.get("dimension").dimension(server, sender).require();
        AxisAlignedTransform2D transform = parameters.transform("rotation", "mirror").optional().orElse(AxisAlignedTransform2D.ORIGINAL);
        BlockPos pos = parameters.pos("x", "y", "z", sender.getPosition(), false).require();
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
