/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockAreas;
import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.ivtoolkit.world.MockWorld;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.mcopts.commands.CommandSplit;
import ivorius.reccomplex.mcopts.commands.SimpleCommand;
import ivorius.reccomplex.mcopts.commands.parameters.*;
import ivorius.reccomplex.mcopts.commands.parameters.expect.Expect;
import ivorius.reccomplex.mcopts.commands.parameters.expect.MCE;
import ivorius.reccomplex.commands.parameters.RCP;
import ivorius.reccomplex.utils.expression.PositionedBlockExpression;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandSelection extends CommandSplit
{
    // TODO Make virtual

    public ICommand set;

    public CommandSelection()
    {
        add(new Command("clear")
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, Parameters parameters, SelectionOwner owner) throws CommandException
            {
                owner.setSelection(null);
            }
        });

        add(new Command("get")
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, Parameters parameters, SelectionOwner owner) throws CommandException
            {
                sender.sendMessage(RecurrentComplex.translations.format("commands.selectSet.get", RCTextStyle.area(owner.getSelection())));
                if (owner.hasValidSelection())
                    sender.sendMessage(RecurrentComplex.translations.format("commands.selectSet.size", RCTextStyle.size(owner.getSelection().areaSize()), IvVecMathHelper.product(owner.getSelection().areaSize())));
            }
        });

        add(set = new Command("set", () -> Parameters.expect().then(MCE::xyz).required().flag("first").flag("second"))
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, Parameters parameters, SelectionOwner owner) throws CommandException
            {
                boolean first = !parameters.has("second");
                boolean second = !parameters.has("first");
                boolean shiftSecond = false;

                // Assume we want to set both after another
                if (!first && !second)
                {
                    shiftSecond = true;
                    first = true;
                    second = true;
                }

                if (first)
                {
                    if (owner.getSelectedPoint1() == null)
                        owner.setSelectedPoint1(sender.getPosition());

                    owner.setSelectedPoint1(parameters.get(0).to(MCP.pos(owner.getSelectedPoint1(), false)).require());
                }
                if (second)
                {
                    if (owner.getSelectedPoint2() == null)
                        owner.setSelectedPoint2(sender.getPosition());

                    owner.setSelectedPoint2((parameters.get(shiftSecond ? 3 : 0)
                            .to(MCP.pos(owner.getSelectedPoint2(), false)).require()));
                }
            }
        });

        add(new Command("crop", () -> Parameters.expect().then(MCE::block).descriptionU("positioned block expression"))
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, Parameters parameters, SelectionOwner owner) throws CommandException
            {
                MockWorld world = MockWorld.of(sender.getEntityWorld());

                BlockArea area = owner.getSelection();

                PositionedBlockExpression matcher = parameters.get(0).rest(NaP.join()).orElse("").to(RCP.expression(new PositionedBlockExpression(RecurrentComplex.specialRegistry))).require();

                for (EnumFacing direction : EnumFacing.VALUES)
                    while (area != null && sideStream(area, direction).allMatch(p -> matcher.test(PositionedBlockExpression.Argument.at(world, p))))
                        area = BlockAreas.shrink(area, direction, 1);

                owner.setSelection(area);
            }
        });

        add(new Command("wand", () -> Parameters.expect().then(MCE::block).descriptionU("positioned block expression"))
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, Parameters parameters, SelectionOwner owner) throws CommandException
            {
                MockWorld world = MockWorld.of(sender.getEntityWorld());
                BlockArea area = owner.getSelection();

                boolean changed = true;
                int total = 0;

                while (changed)
                {
                    changed = false;

                    PositionedBlockExpression matcher = parameters.get(0).rest(NaP.join()).orElse("!is:air").to(RCP.expression(new PositionedBlockExpression(RecurrentComplex.specialRegistry))).require();

                    for (EnumFacing direction : EnumFacing.VALUES)
                    {
                        BlockArea expand;

                        while (sideStream((expand = BlockAreas.expand(area, direction, 1)), direction).anyMatch(p -> matcher.test(PositionedBlockExpression.Argument.at(world, p))) && (total++) < 300)
                        {
                            area = expand;
                            changed = true;
                        }
                    }
                }

                owner.setSelection(area);
            }
        });

        add(new Command("shrink", () -> Parameters.expect()
                .any("1", "2", "3").descriptionU("all")
                .named("x").any("1", "2", "3").descriptionU("x")
                .named("y").any("1", "2", "3").descriptionU("y")
                .named("z").any("1", "2", "3").descriptionU("z")
        )
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, Parameters parameters, SelectionOwner owner) throws CommandException
            {
                BlockPos base = parameters.get(0).to(MCP.pos(parameters.get(0), parameters.get(0), BlockPos.ORIGIN, false)).require();
                BlockPos shrink = parameters.get(MCP.pos("x", "y", "z", base, false)).require();

                owner.setSelection(BlockAreas.shrink(owner.getSelection(), shrink, shrink));
            }
        });

        add(new Command("expand", () -> Parameters.expect()
                .any("1", "2", "3").descriptionU("all")
                .named("x").any("1", "2", "3").descriptionU("x")
                .named("y").any("1", "2", "3").descriptionU("y")
                .named("z").any("1", "2", "3").descriptionU("z")
        )
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, Parameters parameters, SelectionOwner owner) throws CommandException
            {
                BlockPos base = parameters.get(0).to(MCP.pos(parameters.get(0), parameters.get(0), BlockPos.ORIGIN, false)).require();
                BlockPos shrink = parameters.get(MCP.pos("x", "y", "z", base, false)).require();

                owner.setSelection(BlockAreas.expand(owner.getSelection(), shrink, shrink));
            }
        });

        permitFor(2);
    }

    @Nonnull
    protected static Stream<BlockPos> sideStream(BlockArea area, EnumFacing direction)
    {
        return StreamSupport.stream(BlockAreas.side(area, direction).spliterator(), false);
    }

    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "selection";
    }

    public static abstract class Command extends SimpleCommand
    {
        public Command(String name)
        {
            super(name);
        }

        public Command(String name, Supplier<Expect> expector)
        {
            super(name, expector);
        }

        public Command(String name, String usage, Supplier<Expect> expector)
        {
            super(name, usage, expector);
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
        {
            SelectionOwner owner = RCCommands.getSelectionOwner(sender, null, false);
            Parameters parameters = Parameters.of(args, null);

            execute(server, sender, parameters, owner);
        }

        public abstract void execute(MinecraftServer server, ICommandSender sender, Parameters parameters, SelectionOwner owner) throws CommandException;
    }
}
