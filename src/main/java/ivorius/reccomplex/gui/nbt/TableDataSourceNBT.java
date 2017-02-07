/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.nbt;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.editstructure.preset.TableDataSourcePresettedList;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.*;
import ivorius.reccomplex.json.NBTTagEndSerializer;
import joptsimple.internal.Strings;
import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by lukas on 17.01.17.
 */
public class TableDataSourceNBT
{
    private static final Object[] TYPE_LOOKUP = new Object[]{
            "End", Constants.NBT.TAG_END,
            "Byte", Constants.NBT.TAG_BYTE,
            "Short", Constants.NBT.TAG_SHORT,
            "Int", Constants.NBT.TAG_INT,
            "Long", Constants.NBT.TAG_LONG,
            "Float", Constants.NBT.TAG_FLOAT,
            "Double", Constants.NBT.TAG_DOUBLE,
            "Bytes", Constants.NBT.TAG_BYTE_ARRAY,
            "String", Constants.NBT.TAG_STRING,
            "List", Constants.NBT.TAG_LIST,
            "Compound", Constants.NBT.TAG_COMPOUND,
            "Ints", Constants.NBT.TAG_INT_ARRAY
    };

    public static TableCell cell(@Nullable NBTBase nbt, @Nonnull TableDelegate delegate, @Nonnull TableNavigator navigator)
    {
        TableCellDefault cell = rawCell(nbt, delegate, navigator);
        cell.setTooltip(Collections.singletonList(nbt != null ? (String) TYPE_LOOKUP[ArrayUtils.indexOf(TYPE_LOOKUP, (int) nbt.getId()) - 1] : "null"));
        return cell;
    }

    @Nonnull
    public static TableCellDefault rawCell(@Nullable NBTBase nbt, @Nonnull TableDelegate delegate, @Nonnull TableNavigator navigator)
    {
        if (nbt == null)
        {
            return new TableCellTitle(null, "null");
        }
        else if (nbt instanceof NBTTagEnd)
        {
            return new TableCellTitle(null, "-");
        }
        else if (nbt instanceof NBTTagByte)
        {
            NBTTagByte cNBT = (NBTTagByte) nbt;
            TableCellStringInt cell = new TableCellStringInt(null, cNBT.getInt());
            cell.addPropertyConsumer(value ->
            {
                if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE)
                {
                    ReflectionHelper.setPrivateValue(NBTTagByte.class, cNBT, (byte) (int) value, "field_74756_a", "data");
                    cell.setValidityState(GuiValidityStateIndicator.State.VALID);
                }
                else
                    cell.setValidityState(GuiValidityStateIndicator.State.INVALID);
            });
            return cell;
        }
        else if (nbt instanceof NBTTagShort)
        {
            NBTTagShort cNBT = (NBTTagShort) nbt;
            TableCellStringInt cell = new TableCellStringInt(null, cNBT.getInt());
            cell.addPropertyConsumer(value ->
            {
                if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE)
                {
                    ReflectionHelper.setPrivateValue(NBTTagShort.class, cNBT, (short) (int) value, "field_74752_a", "data");
                    cell.setValidityState(GuiValidityStateIndicator.State.VALID);
                }
                else
                    cell.setValidityState(GuiValidityStateIndicator.State.INVALID);
            });
            return cell;
        }
        else if (nbt instanceof NBTTagInt)
        {
            NBTTagInt cNBT = (NBTTagInt) nbt;
            TableCellStringInt cell = new TableCellStringInt(null, cNBT.getInt());
            cell.addPropertyConsumer(value -> ReflectionHelper.setPrivateValue(NBTTagInt.class, cNBT, value, "field_74748_a", "data"));
            return cell;
        }
        else if (nbt instanceof NBTTagLong)
        {
            NBTTagLong cNBT = (NBTTagLong) nbt;
            TableCellStringLong cell = new TableCellStringLong(null, cNBT.getLong());
            cell.addPropertyConsumer(value ->
                    ReflectionHelper.setPrivateValue(NBTTagLong.class, cNBT, value, "field_74753_a", "data"));
            return cell;
        }
        else if (nbt instanceof NBTTagFloat)
        {
            NBTTagFloat cNBT = (NBTTagFloat) nbt;
            TableCellStringDouble cell = new TableCellStringDouble(null, cNBT.getDouble());
            cell.addPropertyConsumer(value ->
                    ReflectionHelper.setPrivateValue(NBTTagFloat.class, cNBT, (float) (double) value, "field_74750_a", "data"));
            return cell;
        }
        else if (nbt instanceof NBTTagDouble)
        {
            NBTTagDouble cNBT = (NBTTagDouble) nbt;
            TableCellStringDouble cell = new TableCellStringDouble(null, cNBT.getDouble());
            cell.addPropertyConsumer(value ->
                    ReflectionHelper.setPrivateValue(NBTTagDouble.class, cNBT, value, "field_74755_a", "data"));
            return cell;
        }
        else if (nbt instanceof NBTTagByteArray)
        {
            NBTTagByteArray cNBT = (NBTTagByteArray) nbt;
            TableCellString cell = new TableCellString(null,
                    Strings.join(Arrays.stream(ArrayUtils.toObject(cNBT.getByteArray())).map(String::valueOf).collect(Collectors.toList()), ",")
            );
            cell.setShowsValidityState(true);
            cell.addPropertyConsumer(value ->
            {
                byte[] bytes = parseBytes(value);
                if (bytes == null)
                    cell.setValidityState(GuiValidityStateIndicator.State.INVALID);
                else
                {
                    cell.setValidityState(GuiValidityStateIndicator.State.VALID);
                    ReflectionHelper.setPrivateValue(NBTTagByteArray.class, cNBT, bytes, "field_74754_a", "data");
                }
            });
            return cell;

        }
        else if (nbt instanceof NBTTagString)
        {
            NBTTagString cNBT = (NBTTagString) nbt;
            TableCellString cell = new TableCellString(null, cNBT.getString());
            cell.addPropertyConsumer(value ->
                    ReflectionHelper.setPrivateValue(NBTTagString.class, cNBT, value, "field_74751_a", "data"));
            return cell;
        }
        else if (nbt instanceof NBTTagList)
        {
            return TableCellMultiBuilder.create(navigator, delegate)
                    .addNavigation(() -> new TableDataSourceNBTList(delegate, navigator, (NBTTagList) nbt))
                    .build();
        }
        else if (nbt instanceof NBTTagCompound)
        {
            return TableCellMultiBuilder.create(navigator, delegate)
                    .addNavigation(() -> new TableDataSourceNBTTagCompound(delegate, navigator, (NBTTagCompound) nbt))
                    .build();
        }
        else if (nbt instanceof NBTTagIntArray)
        {
            NBTTagIntArray cNBT = (NBTTagIntArray) nbt;
            TableCellString cell = new TableCellString(null,
                    Strings.join(Arrays.stream(ArrayUtils.toObject(cNBT.getIntArray())).map(String::valueOf).collect(Collectors.toList()), ",")
            );
            cell.setShowsValidityState(true);
            cell.addPropertyConsumer(value ->
            {
                int[] ints = parseInts(value);
                if (ints == null)
                    cell.setValidityState(GuiValidityStateIndicator.State.INVALID);
                else
                {
                    cell.setValidityState(GuiValidityStateIndicator.State.VALID);
                    ReflectionHelper.setPrivateValue(NBTTagIntArray.class, cNBT, ints, "field_74749_a", "intArray");
                }
            });
            return cell;
        }
        else
        {
            RecurrentComplex.logger.error("Unexpected nbt type: " + nbt.getClass());
            throw new InternalError();
        }
    }

    private static byte[] parseBytes(String string)
    {
        if (string.isEmpty())
            return new byte[0];

        String[] strings = string.split(",");
        byte[] bytes = new byte[strings.length];

        for (int i = 0; i < strings.length; i++)
        {
            try
            {
                bytes[i] = Byte.parseByte(strings[i]);
            }
            catch (NumberFormatException e)
            {
                return null;
            }
        }

        return bytes;
    }

    private static int[] parseInts(String string)
    {
        if (string.isEmpty())
            return new int[0];

        String[] strings = string.split(",");
        int[] ints = new int[strings.length];

        for (int i = 0; i < strings.length; i++)
        {
            try
            {
                ints[i] = Integer.parseInt(strings[i]);
            }
            catch (NumberFormatException e)
            {
                return null;
            }
        }

        return ints;
    }

    @Nonnull
    public static TableCellButton addButton(int id, Consumer<NBTBase> consumer)
    {
        TableCellButton button = TableDataSourcePresettedList.addAction(true, (String) TYPE_LOOKUP[id * 2], (String) TYPE_LOOKUP[id * 2], null);
        button.addAction(() -> consumer.accept(typeSupplier(id).get()));
        return button;
    }

    public static Supplier<NBTBase> typeSupplier(int id)
    {
        NBTBase nbt;

        switch (id)
        {
            case Constants.NBT.TAG_END:
                return NBTTagEndSerializer::createNBTTagEnd;
            case Constants.NBT.TAG_BYTE:
                return () -> new NBTTagByte((byte) 0);
            case Constants.NBT.TAG_SHORT:
                return () -> new NBTTagShort((short) 0);
            case Constants.NBT.TAG_INT:
                return () -> new NBTTagInt(0);
            case Constants.NBT.TAG_LONG:
                return () -> new NBTTagLong(0);
            case Constants.NBT.TAG_FLOAT:
                return () -> new NBTTagFloat(0);
            case Constants.NBT.TAG_DOUBLE:
                return () -> new NBTTagDouble(0);
            case Constants.NBT.TAG_BYTE_ARRAY:
                return () -> new NBTTagByteArray(new byte[0]);
            case Constants.NBT.TAG_STRING:
                return NBTTagString::new;
            case Constants.NBT.TAG_LIST:
                return NBTTagList::new;
            case Constants.NBT.TAG_COMPOUND:
                return NBTTagCompound::new;
            case Constants.NBT.TAG_INT_ARRAY:
                return () -> new NBTTagIntArray(new int[0]);
            default:
                throw new IllegalStateException();
        }
    }

    @Nonnull
    public static TableCellButton typeButton(int id, Runnable runnable)
    {
        TableCellButton button = TableDataSourcePresettedList.addAction(true, (String) TYPE_LOOKUP[id * 2], "Set to " + (String) TYPE_LOOKUP[id * 2], null);
        button.addAction(runnable);
        return button;
    }
}
