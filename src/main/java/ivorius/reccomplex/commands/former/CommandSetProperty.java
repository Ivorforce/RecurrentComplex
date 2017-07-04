/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.former;

import ivorius.ivtoolkit.world.MockWorld;
import ivorius.mcopts.commands.SimpleCommand;
import ivorius.mcopts.commands.parameters.Parameters;
import ivorius.mcopts.commands.parameters.expect.Expect;
import ivorius.mcopts.commands.parameters.expect.MCE;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.commands.CommandVirtual;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.commands.parameters.RCP;
import ivorius.reccomplex.utils.expression.PositionedBlockExpression;
import ivorius.reccomplex.world.gen.feature.structure.generic.transformers.TransformerProperty;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.math.BlockPos;

import java.util.function.Consumer;
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
    public void expect(Expect expect)
    {
        expect
                .next(TransformerProperty.propertyNameStream().collect(Collectors.toSet())).descriptionU("key").required()
                .next(params -> params.get(0).tryGet().map(TransformerProperty::propertyValueStream)).descriptionU("value").required()
                .named("exp").words(MCE::block).descriptionU("positioned block expression")
                .named("shape", "s").then(CommandFill::shape);
    }

    @Override
    public void execute(MockWorld world, ICommandSender sender, String[] args) throws CommandException
    {
        Parameters parameters = Parameters.of(args, expect()::declare);

        PositionedBlockExpression matcher = parameters.get("exp").orElse("").to(RCP.expression(new PositionedBlockExpression(RecurrentComplex.specialRegistry))).require();

        String propertyName = parameters.get(0).require();
        String propertyValue = parameters.get(1).require();

        String shape = parameters.get("shape").optional().orElse("cube");

        Consumer<BlockPos> consumer = (BlockPos pos) ->
        {
            PositionedBlockExpression.Argument at = PositionedBlockExpression.Argument.at(world, pos);
            if (matcher.test(at))
                TransformerProperty.withProperty(at.state, propertyName, propertyValue).ifPresent(state -> world.setBlockState(pos, state, 3));
        };
        SelectionOwner selectionOwner = RCCommands.getSelectionOwner(sender, null, true);
        RCCommands.assertSize(sender, selectionOwner);

        CommandFill.runShape(shape, selectionOwner.getSelection(), consumer);
    }
}
