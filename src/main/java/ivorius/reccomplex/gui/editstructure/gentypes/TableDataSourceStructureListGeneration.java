/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import ivorius.ivtoolkit.blocks.Directions;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.RCGuiTables;
import ivorius.reccomplex.gui.TableDataSourceBlockPos;
import ivorius.reccomplex.gui.TableDirections;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.cell.TableCellEnum;
import ivorius.reccomplex.gui.table.cell.TableCellString;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.feature.structure.Structures;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.ListGeneration;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 07.10.14.
 */
public class TableDataSourceStructureListGeneration extends TableDataSourceSegmented
{
    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    private ListGeneration generationInfo;

    public TableDataSourceStructureListGeneration(TableNavigator navigator, TableDelegate tableDelegate, ListGeneration generationInfo)
    {
        this.navigator = navigator;
        this.tableDelegate = tableDelegate;
        this.generationInfo = generationInfo;

        addManagedSegment(0, new TableDataSourceGenerationType(generationInfo, navigator, tableDelegate));
        addManagedSegment(3, new TableDataSourceBlockPos(generationInfo.shift, generationInfo::setShift, null, null, null,
                IvTranslations.get("reccomplex.generationInfo.structureList.shift.x"), IvTranslations.get("reccomplex.generationInfo.structureList.shift.y"), IvTranslations.get("reccomplex.generationInfo.structureList.shift.z")));
    }

    @Override
    public int numberOfSegments()
    {
        return 5;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
            case 1:
                return 1;
            case 2:
                return 1;
            case 4:
                return 1;
        }
        return super.sizeOfSegment(segment);
    }

    @Override
    public TableCell cellForIndexInSegment(GuiTable table, int index, int segment)
    {
        switch (segment)
        {
            case 1:
            {
                TableCellString cell = new TableCellString("listID", generationInfo.listID);
                cell.setShowsValidityState(true);
                cell.setValidityState(currentStructureListIDState());
                cell.addPropertyConsumer(cell1 ->
                {
                    generationInfo.listID = cell.getPropertyValue();
                    cell.setValidityState(currentStructureListIDState());
                });
                return new TitledCell(IvTranslations.get("reccomplex.generationInfo.structureList.id"), cell);
            }
            case 2:
                return RCGuiTables.defaultWeightElement(val -> generationInfo.weight = TableCells.toDouble(val), generationInfo.weight);
            case 4:
            {
                TableCellEnum<EnumFacing> cell = new TableCellEnum<>("front", generationInfo.front, TableDirections.getDirectionOptions(Directions.HORIZONTAL));
                cell.addPropertyConsumer(cell1 -> generationInfo.front = (EnumFacing) cell.getPropertyValue());
                return new TitledCell(IvTranslations.get("reccomplex.generationInfo.structureList.front"), cell);
            }
        }

        return super.cellForIndexInSegment(table, index, segment);
    }

    @Nonnull
    protected GuiValidityStateIndicator.State currentStructureListIDState()
    {
        return Structures.isSimpleID(generationInfo.listID)
                ? StructureRegistry.INSTANCE.getStructuresInList(generationInfo.listID, null).count() > 0
                ? GuiValidityStateIndicator.State.VALID
                : GuiValidityStateIndicator.State.SEMI_VALID
                : GuiValidityStateIndicator.State.INVALID;
    }
}
