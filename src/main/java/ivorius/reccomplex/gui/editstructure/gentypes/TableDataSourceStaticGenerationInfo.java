/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.TableDataSourceBlockSurfacePos;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.editstructure.TableDataSourceYSelector;
import ivorius.reccomplex.gui.editstructure.gentypes.staticgen.TableDataSourceStaticPattern;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.gentypes.StaticGenerationInfo;

/**
 * Created by lukas on 07.10.14.
 */
public class TableDataSourceStaticGenerationInfo extends TableDataSourceSegmented implements TableCellPropertyListener
{
    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    private StaticGenerationInfo generationInfo;

    public TableDataSourceStaticGenerationInfo(TableNavigator navigator, TableDelegate tableDelegate, StaticGenerationInfo generationInfo)
    {
        this.navigator = navigator;
        this.tableDelegate = tableDelegate;
        this.generationInfo = generationInfo;

        addManagedSection(0, new TableDataSourceGenerationInfo(generationInfo, navigator, tableDelegate));
        addManagedSection(2, new TableDataSourceBlockSurfacePos(generationInfo.position, generationInfo::setPosition, null, null,
                IvTranslations.get("reccomplex.generationInfo.static.position.x"), IvTranslations.get("reccomplex.generationInfo.static.position.z")));
        addManagedSection(3, new TableDataSourceYSelector(generationInfo.ySelector));
        addManagedSection(4, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.gui.biomes"), generationInfo.dimensionMatcher));

        addManagedSection(5, TableCellMultiBuilder.create(navigator, tableDelegate)
                .addNavigation(() -> IvTranslations.get("reccomplex.gui.edit"), null,
                        () -> new GuiTable(tableDelegate, new TableDataSourceStaticPattern(generationInfo.pattern, tableDelegate))
                ).enabled(generationInfo::hasPattern)
                .addAction(() -> generationInfo.hasPattern() ? IvTranslations.get("reccomplex.gui.remove") : IvTranslations.get("reccomplex.gui.add"), null,
                        () -> generationInfo.pattern = generationInfo.hasPattern() ? null : new StaticGenerationInfo.Pattern()
                ).buildDataSource(IvTranslations.get("reccomplex.generationInfo.static.pattern")));
    }

    @Override
    public int numberOfSegments()
    {
        return 6;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return segment == 1 ? 1 : super.sizeOfSegment(segment);
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        switch (segment)
        {
            case 1:
            {
                TableCellBoolean cell = new TableCellBoolean("relativeToSpawn", generationInfo.relativeToSpawn);
                cell.addPropertyListener(this);
                return new TableElementCell(IvTranslations.get("reccomplex.generationInfo.static.spawn"), cell);
            }
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    @Override
    public void valueChanged(TableCellPropertyDefault cell)
    {
        if (cell.getID() != null)
        {
            switch (cell.getID())
            {
                case "relativeToSpawn":
                    generationInfo.relativeToSpawn = (boolean) cell.getPropertyValue();
                    break;
            }
        }
    }
}
