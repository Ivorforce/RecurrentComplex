/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.clipboard;

import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.capability.RCEntityInfo;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.commands.parameters.CommandExpecting;
import ivorius.reccomplex.commands.parameters.Expect;
import ivorius.reccomplex.commands.parameters.RCExpect;
import ivorius.reccomplex.commands.parameters.RCParameters;
import ivorius.reccomplex.operation.OperationRegistry;
import ivorius.reccomplex.utils.RCBlockAreas;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.StructureGenerator;
import ivorius.reccomplex.world.gen.feature.structure.OperationGenerateStructure;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.StructureBoundingBox;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandPaste extends CommandExpecting
{
    public CommandPaste()
    {
    }

    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "paste";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public Expect<?> expect()
    {
        return RCExpect.expectRC()
                .pos("x", "y", "z")
                .named("rotation", "r").rotation()
                .named("seed").randomString()
                .flag("mirror", "m")
                .flag("generate", "g")
                .flag("select", "s");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args, expect()::declare);

        EntityPlayerMP entityPlayerMP = getCommandSenderAsPlayer(sender);
        RCEntityInfo entityInfo = RCCommands.getStructureEntityInfo(entityPlayerMP, null);

        NBTTagCompound worldData = entityInfo.getWorldDataClipboard();

        if (worldData == null)
            throw ServerTranslations.commandException("commands.strucPaste.noClipboard");

        WorldServer world = (WorldServer) sender.getEntityWorld();
        BlockPos pos = parameters.pos("x", "y", "z", sender.getPosition(), false).require();
        AxisAlignedTransform2D transform = parameters.transform("rotation", "mirror").optional().orElse(AxisAlignedTransform2D.ORIGINAL);
        String seed = parameters.get("seed").optional().orElse(null);
        boolean generate = parameters.has("generate");
        boolean select = parameters.has("select");

        GenericStructure structure = GenericStructure.createDefaultStructure();
        structure.worldDataCompound = worldData;

        // TODO Generate with generation info?
        OperationRegistry.queueOperation(new OperationGenerateStructure(structure, null, transform, pos, generate)
                .withSeed(seed)
                .prepare(world), sender);

        if (select)
        {
            StructureGenerator<?> generator = new StructureGenerator<>(structure).transform(transform).lowerCoord(pos);

            // Can never not place so don't handle
            //noinspection OptionalGetWithoutIsPresent
            StructureBoundingBox boundingBox = generator.boundingBox().get();

            SelectionOwner owner = RCCommands.getSelectionOwner(sender, null, false);
            owner.setSelection(RCBlockAreas.from(boundingBox));
        }
    }
}
