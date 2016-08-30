/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui;

import com.google.common.collect.Lists;
import net.minecraft.util.text.TextFormatting;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.editstructure.transformers.TableDataSourceBTNatural;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.utils.BlockStates;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by lukas on 06.05.16.
 */
public class TableDataSourceBlockState extends TableDataSourceSegmented implements TableCellPropertyListener
{
    private String block = "";
    private int meta;

    private Consumer<IBlockState> consumer;

    private String blockTitle;
    private String metadataTitle;

    private TableCellString idCell;
    private TableCellInteger metaCell;

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
    }

    public TableDataSourceBlockState(IBlockState state, Consumer<IBlockState> consumer, TableNavigator navigator, TableDelegate delegate)
    {
        this(state, consumer, navigator, delegate, IvTranslations.get("reccomplex.gui.block"), IvTranslations.get("reccomplex.gui.metadata"));
    }

    public TableDataSourceBlockState setShowExtendedProperties(boolean showExtendedProperties)
    {
        this.showExtendedProperties = showExtendedProperties;
        return this;
    }

    public boolean isShowExtendedProperties()
    {
        return showExtendedProperties;
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
            case 0:
                return 2;
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
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        switch (segment)
        {
            case 0:
                if (index == 0)
                {
                    TableCellString cell = idCell = TableDataSourceBTNatural.elementForBlock("block", block);
                    cell.addPropertyListener(this);
                    return new TableElementCell("blockID", blockTitle, cell);
                }
                else if (index == 1)
                {
                    TableCellInteger cell = metaCell = new TableCellInteger("metadata", meta, 0, 15);
                    cell.addPropertyListener(this);
                    return new TableElementCell("blockMeta", metadataTitle, cell);
                }
                break;
            case 1:
                return getPropertyElement(index, false);
            case 2:
                return getPropertyElement(index, true);
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    @Nonnull
    protected TableElement getPropertyElement(int index, boolean extended)
    {
        IBlockState state = computeBlockState();

        IProperty<?> name = getSortedPropertyNames(state, extended).get(index);
        List<Comparable<?>> properties = getSortedProperties((IProperty) name);
        Comparable<?> currentProperty = state.getValue((IProperty) name);

        if (properties.size() <= 4)
        {
            List<TableCellButton> buttons = properties.stream().map(property ->
            {
                TableCellButton button = new TableCellButton(null, null, (property == currentProperty ? TextFormatting.GREEN : "") + ((IProperty) name).getName(property));
                button.setEnabled(!extended);
                button.addListener((cell, action) ->
                {
                    setBlockStateAndNotify(state.withProperty((IProperty) name, (Comparable) property));
                    delegate.reloadData();
                });
                if (property == currentProperty)
                    button.setEnabled(false);
                return button;
            }).collect(Collectors.toList());
            return new TableElementCell(name.getName(), new TableCellMulti(buttons));
        }

        TableCellButton button = new TableCellButton(null, null, TextFormatting.GREEN + ((IProperty) name).getName(currentProperty));
        button.setEnabled(!extended);
        button.addListener((cell, action) ->
        {
            setBlockStateAndNotify(state.cycleProperty((IProperty) name));
            delegate.reloadData();
        });
        return new TableElementCell(name.getName(), button);
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
        List<IProperty<?>> names = Lists.newArrayList(state.getPropertyNames());
        // Remove if it doesn't make a difference on metadata -> isn't saved
        names.removeIf(name -> (name.getAllowedValues().stream()
                .mapToInt(obj -> BlockStates.toMetadata(state.withProperty((IProperty) name, (Comparable) obj)))
                .distinct().count() < 2) != extended);
        Collections.sort(names, (o1, o2) -> o1.getName().compareTo(o2.getName()));
        return names;
    }

    @Override
    public void valueChanged(TableCellPropertyDefault cell)
    {
        IBlockState before = computeBlockState();
        IBlockState state;
        block = idCell.getPropertyValue();
        meta = metaCell.getPropertyValue();

        TableDataSourceBTNatural.setStateForBlockTextfield(idCell);

        setBlockStateAndNotify(state = computeBlockState());
        if (state != before)
            TableElements.reloadExcept(delegate, "blockID", "blockMeta");
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
            return block != null ? block.getStateFromMeta(meta) : null;
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
