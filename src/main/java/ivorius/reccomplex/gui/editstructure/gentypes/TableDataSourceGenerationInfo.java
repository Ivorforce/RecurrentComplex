/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.*;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.feature.structure.Structures;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.GenerationType;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 26.03.15.
 */
public class TableDataSourceGenerationInfo extends TableDataSourceSegmented
{
    public GenerationType genInfo;

    public TableDelegate delegate;

    public TableDataSourceGenerationInfo(GenerationType genInfo, TableNavigator navigator, TableDelegate delegate)
    {
        this.genInfo = genInfo;
        this.delegate = delegate;
    }

    @Nonnull
    @Override
    public String title()
    {
        return genInfo.displayString();
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
    public TableCell cellForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            TableCellString idCell = new TableCellString("genInfoID", genInfo.id());
            idCell.setShowsValidityState(true);
            idCell.setValidityState(currentIDState());
            idCell.addPropertyConsumer(val ->
            {
                genInfo.setID(val);
                idCell.setValidityState(currentIDState());
            });

            TableCellButton randomizeCell = new TableCellButton(null, null, IvTranslations.get("reccomplex.gui.randomize.short"), IvTranslations.getLines("reccomplex.gui.randomize"));
            randomizeCell.addAction(() -> {
                genInfo.setID(GenerationType.randomID(genInfo.getClass()));
                delegate.reloadData();
            });

            TableCellMulti cell = new TableCellMulti(idCell, randomizeCell);
            cell.setSize(1, 0.1f);
            return new TitledCell(IvTranslations.get("reccomplex.structure.generation.id"), cell)
                    .withTitleTooltip(IvTranslations.formatLines("reccomplex.structure.generation.id.tooltip"));
        }

        return super.cellForIndexInSegment(table, index, segment);
    }

    protected GuiValidityStateIndicator.State currentIDState()
    {
        return Structures.isSimpleIDState(genInfo.id());
    }
}
