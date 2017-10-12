/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui;

import com.google.common.collect.Lists;
import ivorius.ivtoolkit.blocks.BlockStates;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.editstructure.transformers.TableDataSourceBTNatural;
import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.gui.table.TableCells;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.*;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by lukas on 06.05.16.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceBlockState extends TableDataSourceSegmented
{
    private String block = "";
    private int meta;

    private Consumer<IBlockState> consumer;

    private String blockTitle;
    private String metadataTitle;

    private TableCellString idCell;
    private TableCellIntSlider metaCell;

    private TableNavigator navigator;
    private TableDelegate delegate;

    private boolean showExtendedProperties;

    public TableDataSourceBlockState(IBlockState state, Consumer<IBlockState> consumer, TableNavigator navigator, TableDelegate delegate, String blockTitle, String metadataTitle)
    {
        setBlockState(state);
        this.consumer = consumer;
        this.blockTitle = blockTitle;
        this.metadataTitle = metadataTitle;
        this.navigator = navigator;
        this.delegate = delegate;

        addSegment(0, () -> {
            TableCellString cell = idCell = TableDataSourceBTNatural.cellForBlock("block", block);
            cell.addListener(p -> valueChanged());
            return new TitledCell("blockID", blockTitle, cell);
        }, () -> {
            TableCellIntSlider cell = metaCell = new TableCellIntSlider("metadata", meta, 0, 15);
            cell.addListener(p -> valueChanged());
            return new TitledCell("blockMeta", metadataTitle, cell);
        });
    }

    public TableDataSourceBlockState(IBlockState state, Consumer<IBlockState> consumer, TableNavigator navigator, TableDelegate delegate)
    {
        this(state, consumer, navigator, delegate, IvTranslations.get("reccomplex.gui.block"), IvTranslations.get("reccomplex.gui.metadata"));
    }

    public boolean isShowExtendedProperties()
    {
        return showExtendedProperties;
    }

    public TableDataSourceBlockState setShowExtendedProperties(boolean showExtendedProperties)
    {
        this.showExtendedProperties = showExtendedProperties;
        return this;
    }

    @Override
    public int numberOfSegments()
    {
        return showExtendedProperties ? 3 : 2;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
            case 1:
            {
                IBlockState state = computeBlockState();
                return state != null ? getSortedPropertyNames(state, false).size() : 0;
            }
            case 2:
            {
                IBlockState state = computeBlockState();
                return state != null ? getSortedPropertyNames(state, true).size() : 0;
            }
        }
        return super.sizeOfSegment(segment);
    }

    @Override
    public TableCell cellForIndexInSegment(GuiTable table, int index, int segment)
    {
        switch (segment)
        {
            case 1:
                return getPropertyElement(index, false);
            case 2:
                return getPropertyElement(index, true);
        }

        return super.cellForIndexInSegment(table, index, segment);
    }

    @Nonnull
    protected <T extends Comparable<T>> TitledCell getPropertyElement(int index, boolean extended)
    {
        IBlockState state = computeBlockState();

        @SuppressWarnings("unchecked") IProperty<T> name = (IProperty<T>) getSortedPropertyNames(state, extended).get(index);
        List<T> properties = getSortedProperties(name);
        T currentProperty = state.getValue(name);

        if (properties.size() <= 4)
        {
            List<TableCellButton> buttons = properties.stream().map(property ->
            {
                TableCellButton button = new TableCellButton(null, null, name.getName(property));
                button.setEnabled(!extended);
                button.addAction(() ->
                {
                    setBlockStateAndNotify(state.withProperty(name, property));
                    delegate.reloadData();
                });
                if (property == currentProperty)
                    button.setEnabled(false);
                return button;
            }).collect(Collectors.toList());
            return new TitledCell(name.getName(), new TableCellMulti(buttons));
        }

        List<T> sorted = Lists.newArrayList(name.getAllowedValues());
        Collections.sort(sorted);

        TableCellEnum<T> cell = new TableCellEnum<>(null, (T) state.getValue(name), sorted.stream()
                .map(t1 -> new TableCellEnum.Option<>(t1, name.getName(currentProperty))).collect(Collectors.toList()));
        cell.addListener(t -> {
            setBlockStateAndNotify(state.withProperty(name, t));
            delegate.reloadData();
        });
        cell.setEnabled(!extended);
        return new TitledCell(name.getName(), cell);
    }

    protected <T extends Comparable<T>> List<T> getSortedProperties(IProperty<T> name)
    {
        List<T> values = Lists.newArrayList(name.getAllowedValues());
        Collections.sort(values);
        return values;
    }

    @Nonnull
    protected List<IProperty<?>> getSortedPropertyNames(IBlockState state, boolean extended)
    {
        List<IProperty<?>> names = Lists.newArrayList(state.getPropertyKeys());
        // Remove if it doesn't make a difference on metadata -> isn't saved
        //noinspection unchecked
        names.removeIf(name -> (name.getAllowedValues().stream()
                .mapToInt(obj -> BlockStates.toMetadata(state.withProperty((IProperty) name, (Comparable) obj)))
                .distinct().count() < 2) != extended);
        names.sort(Comparator.comparing(IProperty::getName));
        return names;
    }

    public void valueChanged()
    {
        IBlockState before = computeBlockState();
        IBlockState state;
        block = idCell.getPropertyValue();
        meta = metaCell.getPropertyValue();

        TableDataSourceBTNatural.setStateForBlockTextfield(idCell);

        setBlockStateAndNotify(state = computeBlockState());
        if (state != before)
            TableCells.reloadExcept(delegate, "blockID", "blockMeta");
    }

    protected void setBlockStateAndNotify(IBlockState c)
    {
        if (c != null)
        {
            consumer.accept(c);
            setBlockState(c);
        }
    }

    public void setBlockState(IBlockState state)
    {
        this.block = Block.REGISTRY.getNameForObject(state.getBlock()).toString();
        this.meta = BlockStates.toMetadata(state);
    }

    public IBlockState computeBlockState()
    {
        try
        {
            Block block = RecurrentComplex.mcRegistry.blockFromID(new ResourceLocation(this.block));
            return block != null ? BlockStates.fromMetadata(block, meta) : null;
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
