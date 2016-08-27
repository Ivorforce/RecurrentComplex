/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.StructureInfos;
import ivorius.reccomplex.structures.generic.gentypes.StructureGenerationInfo;

/**
 * Created by lukas on 26.03.15.
 */
public class TableDataSourceGenerationInfo extends TableDataSourceSegmented
{
    public StructureGenerationInfo genInfo;

    public TableDataSourceGenerationInfo(StructureGenerationInfo genInfo, TableNavigator navigator, TableDelegate delegate)
    {
        this.genInfo = genInfo;
        addManagedSection(1, TableCellMultiBuilder.create(navigator, delegate)
                .addAction(() -> "Randomize", null,
                        () -> genInfo.setID(StructureGenerationInfo.randomID()))
                .buildPreloaded());
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
            TableCellString cell = new TableCellString("genInfoID", genInfo.id());
            cell.setTooltip(IvTranslations.formatLines("reccomplex.structure.generation.id.tooltip"));
            cell.setShowsValidityState(true);
            cell.setValidityState(StructureInfos.defaultIDValidityState(genInfo));
            cell.addPropertyListener(cell1 ->
            {
                genInfo.setID((String) cell1.getPropertyValue());
                ((TableCellString) cell1).setValidityState(StructureInfos.defaultIDValidityState(genInfo));
            });
            return new TableElementCell(IvTranslations.get("reccomplex.structure.generation.id"), cell);
        }

        return super.elementForIndexInSegment(table, index, segment);
    }
}
