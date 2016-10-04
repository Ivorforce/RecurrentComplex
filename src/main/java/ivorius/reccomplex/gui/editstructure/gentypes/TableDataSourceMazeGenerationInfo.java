/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.RCGuiTables;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCellString;
import ivorius.reccomplex.gui.table.cell.TableElementCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.worldscripts.mazegenerator.TableDataSourceMazeComponent;
import ivorius.reccomplex.world.gen.feature.structure.generic.gentypes.MazeGenerationInfo;

/**
 * Created by lukas on 07.10.14.
 */
public class TableDataSourceMazeGenerationInfo extends TableDataSourceSegmented
{
    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    private MazeGenerationInfo generationInfo;

    public TableDataSourceMazeGenerationInfo(TableNavigator navigator, TableDelegate tableDelegate, MazeGenerationInfo generationInfo)
    {
        this.navigator = navigator;
        this.tableDelegate = tableDelegate;
        this.generationInfo = generationInfo;

        addManagedSegment(0, new TableDataSourceGenerationInfo(generationInfo, navigator, tableDelegate));
        addManagedSegment(3, new TableDataSourceMazeComponent(generationInfo.mazeComponent, true, navigator, tableDelegate));
    }

    @Override
    public int numberOfSegments()
    {
        return 4;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
            case 1:
            case 2:
                return 1;
            default:
                return super.sizeOfSegment(segment);
        }

    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        switch (segment)
        {
            case 1:
            {
                TableCellString cell = new TableCellString("mazeID", generationInfo.mazeID);
                cell.addPropertyConsumer(generationInfo::setID);
                return new TableElementCell(IvTranslations.get("reccomplex.generationInfo.mazeComponent.mazeid"), cell);
            }
            case 2:
            {
                return RCGuiTables.defaultWeightElement(val -> generationInfo.weight = TableElements.toDouble(val), generationInfo.weight);
            }
        }

        return super.elementForIndexInSegment(table, index, segment);
    }
}
