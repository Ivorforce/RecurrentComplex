/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.transformers;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.StructureInfos;
import ivorius.reccomplex.structures.generic.transformers.Transformer;

/**
 * Created by lukas on 29.08.16.
 */
public class TableDataSourceTransformer extends TableDataSourceSegmented
{
    public Transformer transformer;

    public TableDataSourceTransformer(Transformer transformer, TableDelegate delegate, TableNavigator navigator)
    {
        this.transformer = transformer;
        addManagedSection(1, TableCellMultiBuilder.create(navigator, delegate)
                .addAction(() -> IvTranslations.get("reccomplex.gui.randomize"), null,
                        () -> transformer.setID(Transformer.randomID(transformer.getClass())))
                .buildDataSource());
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
            TableCellString cell = new TableCellString("transformerID", transformer.id());
            cell.setTooltip(IvTranslations.formatLines("reccomplex.transformer.id.tooltip"));
            cell.setShowsValidityState(true);
            cell.setValidityState(currentIDState());
            cell.addPropertyConsumer(val ->
            {
                transformer.setID(val);
                cell.setValidityState(currentIDState());
            });
            return new TableElementCell(IvTranslations.get("reccomplex.transformer.id"), cell);
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    protected GuiValidityStateIndicator.State currentIDState()
    {
        return StructureInfos.isSimpleIDState(transformer.id());
    }
}
