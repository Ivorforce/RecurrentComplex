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
public class TableDataSourceGenerationInfo implements TableDataSource, TableElementPropertyListener
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
        TableElementString element = new TableElementString("genInfoID", IvTranslations.get("reccomplex.structure.generation.id"), genInfo.id());
        element.setTooltip(IvTranslations.formatLines("reccomplex.structure.generation.id.tooltip"));
        element.setShowsValidityState(true);
        element.setValidityState(StructureInfos.defaultIDValidityState(genInfo));
        element.addPropertyListener(this);
        return element;
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        if ("genInfoID".equals(element.getID()))
        {
            genInfo.setID((String) element.getPropertyValue());
            ((TableElementString) element).setValidityState(StructureInfos.defaultIDValidityState(genInfo));
        }
    }
}
