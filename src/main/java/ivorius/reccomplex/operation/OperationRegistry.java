/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.operation;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import ivorius.ivtoolkit.lang.IvClasses;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.ivtoolkit.tools.NBTCompoundObjects;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.capability.RCEntityInfo;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.utils.RCPacketBuffer;
import ivorius.reccomplex.world.gen.feature.structure.OperationClearArea;
import ivorius.reccomplex.world.gen.feature.structure.OperationGenerateStructure;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by lukas on 10.02.15.
 */
public class OperationRegistry
{
    private static BiMap<String, Class<? extends Operation>> operations = HashBiMap.create();

    public static void register(String id, Class<? extends Operation> operation)
    {
        operations.put(id, operation);
    }

    @Nullable
    public static Operation readOperation(@Nonnull NBTTagCompound compound)
    {
        String opID = compound.getString("opID");
        Class<? extends Operation> clazz = operations.get(opID);

        if (clazz == null)
        {
            RecurrentComplex.logger.error(String.format("Unrecognized Operation ID '%s'", opID));
            return null;
        }

        return NBTCompoundObjects.read(compound, () -> IvClasses.instantiate(clazz));
    }

    public static NBTTagCompound writeOperation(@Nonnull Operation operation)
    {
        NBTTagCompound compound = NBTCompoundObjects.write(operation);
        compound.setString("opID", operations.inverse().get(operation.getClass()));
        return compound;
    }

    public static void queueOperation(Operation operation, ICommandSender commandSender) throws PlayerNotFoundException
    {
        boolean instant = true;

        if (commandSender instanceof EntityPlayer)
        {
            EntityPlayer player = CommandBase.getCommandSenderAsPlayer(commandSender);
            RCEntityInfo info = RCEntityInfo.get(player, null);
            if (info != null)
            {
                if (info.getPreviewType() != Operation.PreviewType.NONE)
                {
                    info.queueOperation(operation, player);
                    instant = false;

                    ITextComponent confirmComponent = new TextComponentString("/" + RCCommands.confirm.getName());
                    confirmComponent.getStyle().setColor(TextFormatting.GREEN);
                    confirmComponent.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + RCCommands.confirm.getName()));
                    confirmComponent.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, RecurrentComplex.translations.get("commands.rcconfirm.run")));

                    ITextComponent cancelComponent = new TextComponentString("/" + RCCommands.cancel.getName());
                    cancelComponent.getStyle().setColor(TextFormatting.RED);
                    cancelComponent.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + RCCommands.cancel.getName()));
                    cancelComponent.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, RecurrentComplex.translations.get("commands.rccancel.run")));

                    commandSender.sendMessage(RecurrentComplex.translations.format("commands.rc.queuedOp", confirmComponent, cancelComponent));
                }
            }
        }

        if (instant)
            operation.perform((WorldServer) commandSender.getEntityWorld());
    }

    @Nullable
    public static NBTTagCompound dummyOperation(@Nonnull Operation operation, int level)
    {
        if (level == 0)
            return writeOperation(operation);

        if (operation instanceof OperationGenerateStructure)
        {
            OperationGenerateStructure genStructure = (OperationGenerateStructure) operation;

            if (level == 1 && genStructure.structure != null)
            {
                IvWorldData dummyWorldData = genStructure.structure.constructWorldData();
                dummyWorldData.entities.clear();
                dummyWorldData.tileEntities.clear();
                for (BlockPos pos : dummyWorldData.blockCollection.area())
                    dummyWorldData.blockCollection.setBlockState(pos, (dummyWorldData.blockCollection.getBlockState(pos).isNormalCube() ? Blocks.STONE : Blocks.AIR).getDefaultState());

                GenericStructure dummyStructure = new GenericStructure();
                dummyStructure.worldDataCompound = dummyWorldData.createTagCompound();
                return writeOperation(new OperationGenerateStructure(dummyStructure, genStructure.generationInfoID, genStructure.transform, genStructure.lowerCoord, genStructure.generateAsSource));
            }
            else if (level == 2)
                return writeOperation(new OperationClearArea(genStructure.generationArea()));
        }

        return null;
    }

    public static void writeBestPreview(ByteBuf buffer, Operation operation)
    {
        if (operation == null)
        {
            ByteBufUtils.writeTag(buffer, null);
            return;
        }

        NBTTagCompound nbt;
        int level = 0;
        while ((nbt = dummyOperation(operation, level++)) != null)
        {
            if (canSend(nbt))
            {
                new RCPacketBuffer(buffer).writeCompoundTag(nbt);
                return;
            }
        }

        new RCPacketBuffer(buffer).writeCompoundTag(null);
    }

    protected static boolean canSend(NBTTagCompound danglingNBT)
    {
        ByteBuf temp = Unpooled.buffer();
        new RCPacketBuffer(temp).writeCompoundTag(danglingNBT);

        return canSend(temp);
    }

    protected static boolean canSend(ByteBuf temp)
    {
        // From SPacketCustomPayload
        return temp.writerIndex() <= (1048576 * 4 / 5);
    }
}
