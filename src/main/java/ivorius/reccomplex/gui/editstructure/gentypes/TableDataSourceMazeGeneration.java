/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.RCGuiTables;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.cell.TableCellString;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.worldscripts.mazegenerator.TableDataSourceMazeComponent;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.MazeGeneration;

/**
 * Created by lukas on 07.10.14.
 */
public class TableDataSourceMazeGeneration extends TableDataSourceSegmented
{
    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    private MazeGeneration generationInfo;

    public TableDataSourceMazeGeneration(TableNavigator navigator, TableDelegate tableDelegate, MazeGeneration generationInfo)
    {
        this.navigator = navigator;
        this.tableDelegate = tableDelegate;
        this.generationInfo = generationInfo;

        addManagedSegment(0, new TableDataSourceGenerationType(generationInfo, navigator, tableDelegate));
        addManagedSegment(3, new TableDataSourceMazeComponent(generationInfo.mazeComponent, navigator, tableDelegate));
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
    public TableCell cellForIndexInSegment(GuiTable table, int index, int segment)
    {
        switch (segment)
        {
            case 1:
            {
                TableCellString cell = new TableCellString("mazeID", generationInfo.mazeID);
                cell.setValidityState(MazeGeneration.idValidity(cell.getPropertyValue()));
                cell.addPropertyConsumer((mazeID) ->
                {
                    generationInfo.setMazeID(mazeID);
                    cell.setValidityState(MazeGeneration.idValidity(cell.getPropertyValue()));
                });
                return new TitledCell(IvTranslations.get("reccomplex.generationInfo.mazeComponent.mazeid"), cell);
            }
            case 2:
            {
                return RCGuiTables.defaultWeightElement(val -> generationInfo.weight = TableCells.toDouble(val), generationInfo.weight);
            }
        }

        return super.cellForIndexInSegment(table, index, segment);
    }
}
