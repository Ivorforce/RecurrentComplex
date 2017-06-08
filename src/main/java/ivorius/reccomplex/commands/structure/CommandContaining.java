/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.structure;

import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.commands.RCTextStyle;
import ivorius.reccomplex.commands.parameters.*;
import ivorius.reccomplex.commands.rcparameters.RCExpect;
import ivorius.reccomplex.commands.rcparameters.RCP;
import ivorius.reccomplex.utils.expression.BlockExpression;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandContaining extends SimpleCommand
{
    public CommandContaining()
    {
        super(RCConfig.commandPrefix + "containing", () -> RCExpect.expectRC()
                .block().descriptionU("block expression").required()
        );
        permitFor(2);
    }

    public static long containedBlocks(Structure structure, BlockExpression matcher)
    {
        if (structure == null)
            return 0;

        IvBlockCollection collection = structure.blockCollection();

        if (collection == null)
            return 0;

        return collection.area().stream()
                .anyMatch(p -> matcher.evaluate(collection.getBlockState(p))) ? 1 : 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        Parameters parameters = Parameters.of(args, expect()::declare);

        BlockExpression matcher = parameters.get(0).to(RCP.expression_(new BlockExpression(RecurrentComplex.specialRegistry))).require();

        CommandSearchStructure.postResultMessage(commandSender,
                RCTextStyle::structure, CommandSearchStructure.search(StructureRegistry.INSTANCE.ids(), name -> containedBlocks(StructureRegistry.INSTANCE.get(name), matcher))
        );
    }
}
