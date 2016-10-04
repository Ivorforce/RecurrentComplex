/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.transformers;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCellButton;
import ivorius.reccomplex.gui.table.cell.TableCellMulti;
import ivorius.reccomplex.gui.table.cell.TableCellString;
import ivorius.reccomplex.gui.table.cell.TableElementCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.feature.structure.StructureInfos;
import ivorius.reccomplex.world.gen.feature.structure.generic.transformers.Transformer;

/**
 * Created by lukas on 29.08.16.
 */
public class TableDataSourceTransformer extends TableDataSourceSegmented
{
    public Transformer transformer;

    public TableDelegate delegate;

    public TableDataSourceTransformer(Transformer transformer, TableDelegate delegate, TableNavigator navigator)
    {
        this.transformer = transformer;

        this.delegate = delegate;
    }

    @Override
    public int numberOfSegments()
    {
        return 1;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return segment == 0 ? 1 : super.sizeOfSegment(segment);
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            TableCellString idCell = new TableCellString("transformerID", transformer.id());
            idCell.setShowsValidityState(true);
            idCell.setValidityState(currentIDState());
            idCell.addPropertyConsumer(val ->
            {
                transformer.setID(val);
                idCell.setValidityState(currentIDState());
            });

            TableCellButton randomizeCell = new TableCellButton(null, null, IvTranslations.get("reccomplex.gui.randomize.short"), IvTranslations.getLines("reccomplex.gui.randomize"));
            randomizeCell.addAction(() -> {
                transformer.setID(Transformer.randomID(transformer.getClass()));
                delegate.reloadData();
            });

            TableCellMulti cell = new TableCellMulti(idCell, randomizeCell);
            cell.setSize(1, 0.1f);
            return new TableElementCell(IvTranslations.get("reccomplex.transformer.id"), cell)
                    .withTitleTooltip(IvTranslations.formatLines("reccomplex.transformer.id.tooltip"));
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    protected GuiValidityStateIndicator.State currentIDState()
    {
        return StructureInfos.isSimpleIDState(transformer.id());
    }
}
