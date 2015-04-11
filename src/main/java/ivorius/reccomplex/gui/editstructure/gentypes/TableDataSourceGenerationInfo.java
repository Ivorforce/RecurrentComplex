/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.StructureInfos;
import ivorius.reccomplex.structures.generic.gentypes.StructureGenerationInfo;
import ivorius.reccomplex.utils.IvTranslations;

/**
 * Created by lukas on 26.03.15.
 */
public class TableDataSourceGenerationInfo implements TableDataSource, TableCellPropertyListener
{
    public StructureGenerationInfo genInfo;

    public TableDataSourceGenerationInfo(StructureGenerationInfo genInfo)
    {
        this.genInfo = genInfo;
    }

    @Override
    public int numberOfElements()
    {
        return 1;
    }

    @Override
    public TableElement elementForIndex(GuiTable table, int index)
    {
        TableCellString cell = new TableCellString("genInfoID", genInfo.id());
        cell.setTooltip(IvTranslations.formatLines("reccomplex.structure.generation.id.tooltip"));
        cell.setShowsValidityState(true);
        cell.setValidityState(StructureInfos.defaultIDValidityState(genInfo));
        cell.addPropertyListener(this);
        return new TableElementCell(IvTranslations.get("reccomplex.structure.generation.id"), cell);
    }

    @Override
    public void valueChanged(TableCellPropertyDefault cell)
    {
        if ("genInfoID".equals(cell.getID()))
        {
            genInfo.setID((String) cell.getPropertyValue());
            ((TableCellString) cell).setValidityState(StructureInfos.defaultIDValidityState(genInfo));
        }
    }
}
