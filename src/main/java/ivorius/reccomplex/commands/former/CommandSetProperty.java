/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.former;

import ivorius.ivtoolkit.blocks.BlockAreas;
import ivorius.ivtoolkit.world.MockWorld;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.commands.CommandVirtual;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.commands.parameters.*;
import ivorius.reccomplex.utils.expression.PositionedBlockExpression;
import ivorius.reccomplex.utils.optional.IvOptional;
import ivorius.reccomplex.world.gen.feature.structure.generic.transformers.TransformerProperty;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.math.BlockPos;

import java.util.stream.Collectors;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSetProperty extends SimpleCommand implements CommandVirtual
{
    public CommandSetProperty()
    {
        super(RCConfig.commandPrefix + "property");
        permitFor(2);
    }

    @Override
    public Expect<?> expect()
    {
        return RCExpect.expectRC()
                .next(TransformerProperty.propertyNameStream().collect(Collectors.toSet())).descriptionU("key").required()
                .next(params -> params.get(0).tryGet().map(TransformerProperty::propertyValueStream)).descriptionU("value").required()
                .named("exp").block().descriptionU("positioned block expression");
    }

    @Override
    public void execute(MockWorld world, ICommandSender sender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args, expect()::declare);

        PositionedBlockExpression matcher = new PositionedBlockExpression(RecurrentComplex.specialRegistry);
        IvOptional.ifAbsent(parameters.get("exp").expression(matcher).optional(), () -> matcher.setExpression(""));

        String propertyName = parameters.get(0).require();
        String propertyValue = parameters.get(1).require();

        SelectionOwner selectionOwner = RCCommands.getSelectionOwner(sender, null, true);
        RCCommands.assertSize(sender, selectionOwner);
        for (BlockPos pos : BlockAreas.mutablePositions(selectionOwner.getSelection()))
        {
            PositionedBlockExpression.Argument at = PositionedBlockExpression.Argument.at(world, pos);
            if (matcher.test(at))
                TransformerProperty.withProperty(at.state, propertyName, propertyValue).ifPresent(state -> world.setBlockState(pos, state, 3));
        }
    }
}
