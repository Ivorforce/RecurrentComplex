/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.clipboard;

import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.mcopts.commands.CommandExpecting;
import ivorius.mcopts.commands.parameters.MCP;
import ivorius.mcopts.commands.parameters.Parameters;
import ivorius.mcopts.commands.parameters.expect.Expect;
import ivorius.mcopts.commands.parameters.expect.MCE;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.capability.RCEntityInfo;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.commands.parameters.IvP;
import ivorius.reccomplex.commands.parameters.expect.RCE;
import ivorius.reccomplex.operation.OperationRegistry;
import ivorius.reccomplex.utils.RCBlockAreas;
import ivorius.reccomplex.world.gen.feature.StructureGenerator;
import ivorius.reccomplex.operation.OperationGenerateStructure;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandPaste extends CommandExpecting
{
    public CommandPaste()
    {
    }

    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "paste";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void expect(Expect expect)
    {
        expect.then(MCE.pos("x", "y", "z"))
                .named("rotation", "r").then(MCE::rotation)
                .named("seed").words(RCE::randomString)
                .flag("mirror", "m")
                .flag("generate", "g")
                .flag("select", "s");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        Parameters parameters = Parameters.of(args, expect()::declare);

        EntityPlayerMP entityPlayerMP = getCommandSenderAsPlayer(sender);
        RCEntityInfo entityInfo = RCCommands.getStructureEntityInfo(entityPlayerMP, null);

        NBTTagCompound worldData = entityInfo.getWorldDataClipboard();

        if (worldData == null)
            throw RecurrentComplex.translations.commandException("commands.strucPaste.noClipboard");

        WorldServer world = (WorldServer) sender.getEntityWorld();
        BlockPos pos = parameters.get(MCP.pos("x", "y", "z", sender.getPosition(), false)).require();
        AxisAlignedTransform2D transform = parameters.get(IvP.transform("rotation", "mirror")).optional().orElse(AxisAlignedTransform2D.ORIGINAL);
        String seed = parameters.get("seed").optional().orElse(null);
        boolean generate = parameters.has("generate");

        GenericStructure structure = GenericStructure.createDefaultStructure();
        structure.worldDataCompound = worldData;

        // TODO Generate with generation info?
        OperationRegistry.queueOperation(new OperationGenerateStructure(structure, null, transform, pos, !generate)
                .withSeed(seed)
                .prepare(world), sender);

        if (parameters.has("select"))
        {
            StructureGenerator<?> generator = new StructureGenerator<>(structure).transform(transform).lowerCoord(pos);
            // Can never not place so don't handle
            //noinspection OptionalGetWithoutIsPresent
            RCCommands.select(sender, RCBlockAreas.from(generator.boundingBox().get()));
        }
    }
}
