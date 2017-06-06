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
import ivorius.reccomplex.commands.parameters.*;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.utils.expression.PositionedBlockExpression;
import ivorius.reccomplex.utils.optional.IvOptional;
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
            public void execute(MinecraftServer server, ICommandSender sender, RCParameters parameters, SelectionOwner owner) throws CommandException
            {
                owner.setSelection(null);
            }
        });

        add(new Command("get")
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, RCParameters parameters, SelectionOwner owner) throws CommandException
            {
                sender.sendMessage(ServerTranslations.format("commands.selectSet.get", RCTextStyle.area(owner.getSelection())));
                if (owner.hasValidSelection())
                    sender.sendMessage(ServerTranslations.format("commands.selectSet.size", RCTextStyle.size(owner.getSelection().areaSize()), IvVecMathHelper.product(owner.getSelection().areaSize())));
            }
        });

        add(set = new Command("set", () -> RCExpect.expectRC().xyz().flag("first").flag("second"))
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, RCParameters parameters, SelectionOwner owner) throws CommandException
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

                    owner.setSelectedPoint1(parameters.mc().pos(owner.getSelectedPoint1(), false).require());
                }
                if (second)
                {
                    if (owner.getSelectedPoint2() == null)
                        owner.setSelectedPoint2(sender.getPosition());

                    owner.setSelectedPoint2(parameters.mc().move(shiftSecond ? 3 : 0).pos(owner.getSelectedPoint2(), false).require());
                }
            }
        });

        add(new Command("crop", () ->
        {
            return RCExpect.expectRC().block().descriptionU("positioned block expression").optional();
        })
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, RCParameters parameters, SelectionOwner owner) throws CommandException
            {
                MockWorld world = MockWorld.of(sender.getEntityWorld());

                BlockArea area = owner.getSelection();

                PositionedBlockExpression matcher = new PositionedBlockExpression(RecurrentComplex.specialRegistry);
                IvOptional.ifAbsent(parameters.rc().expression(matcher).optional(), () -> matcher.setExpression("is:air"));

                for (EnumFacing direction : EnumFacing.VALUES)
                    while (area != null && sideStream(area, direction).allMatch(p -> matcher.test(PositionedBlockExpression.Argument.at(world, p))))
                        area = BlockAreas.shrink(area, direction, 1);

                owner.setSelection(area);
            }
        });

        add(new Command("wand", () ->
        {
            return RCExpect.expectRC().block().descriptionU("positioned block expression").optional();
        })
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, RCParameters parameters, SelectionOwner owner) throws CommandException
            {
                MockWorld world = MockWorld.of(sender.getEntityWorld());
                BlockArea area = owner.getSelection();

                boolean changed = true;
                int total = 0;

                while (changed)
                {
                    changed = false;

                    PositionedBlockExpression matcher = new PositionedBlockExpression(RecurrentComplex.specialRegistry);
                    IvOptional.ifAbsent(parameters.rc().expression(matcher).optional(), () -> matcher.setExpression("!is:air"));

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

        add(new Command("shrink", () ->
        {
            return RCExpect.expectRC()
                    .any("1", "2", "3").descriptionU("all").optional()
                    .named("x").any("1", "2", "3").descriptionU("x").optional()
                    .named("y").any("1", "2", "3").descriptionU("y").optional()
                        .named("z").any("1", "2", "3").descriptionU("z").optional();
        }
        )
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, RCParameters parameters, SelectionOwner owner) throws CommandException
            {
                BlockPos base = parameters.mc().pos(parameters.mc(), parameters.mc(), BlockPos.ORIGIN, false).require();
                BlockPos shrink = parameters.pos("x", "y", "z", base, false).require();

                owner.setSelection(BlockAreas.shrink(owner.getSelection(), shrink, shrink));
            }
        });

        add(new Command("expand", () ->
        {
            return RCExpect.expectRC()
                    .any("1", "2", "3").descriptionU("all").optional()
                    .named("x").any("1", "2", "3").descriptionU("x").optional()
                    .named("y").any("1", "2", "3").descriptionU("y").optional()
                    .named("z").any("1", "2", "3").descriptionU("z").optional();
        }
        )
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, RCParameters parameters, SelectionOwner owner) throws CommandException
            {
                BlockPos base = parameters.mc().pos(parameters.mc(), parameters.mc(), BlockPos.ORIGIN, false).require();
                BlockPos shrink = parameters.pos("x", "y", "z", base, false).require();

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

        public Command(String name, Supplier<Expect<?>> expector)
        {
            super(name, expector);
        }

        public Command(String name, String usage, Supplier<Expect<?>> expector)
        {
            super(name, usage, expector);
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
        {
            SelectionOwner owner = RCCommands.getSelectionOwner(sender, null, false);
            RCParameters parameters = RCParameters.of(args, null);
            execute(server, sender, parameters, owner);
        }

        public abstract void execute(MinecraftServer server, ICommandSender sender, RCParameters parameters, SelectionOwner owner) throws CommandException;
    }
}
