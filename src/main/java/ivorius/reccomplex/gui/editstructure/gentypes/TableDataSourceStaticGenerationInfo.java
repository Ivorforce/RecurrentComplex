/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.TableDataSourceBlockSurfacePos;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.editstructure.placer.TableDataSourcePlacer;
import ivorius.reccomplex.gui.editstructure.gentypes.staticgen.TableDataSourceStaticPattern;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.gentypes.StaticGenerationInfo;

/**
 * Created by lukas on 07.10.14.
 */
public class TableDataSourceStaticGenerationInfo extends TableDataSourceSegmented
{
    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    private StaticGenerationInfo generationInfo;

    public TableDataSourceStaticGenerationInfo(TableNavigator navigator, TableDelegate delegate, StaticGenerationInfo generationInfo)
    {
        this.navigator = navigator;
        this.tableDelegate = delegate;
        this.generationInfo = generationInfo;

        addManagedSegment(0, new TableDataSourceGenerationInfo(generationInfo, navigator, delegate));

        addManagedSegment(2, new TableDataSourceBlockSurfacePos(generationInfo.position, generationInfo::setPosition, null, null,
                IvTranslations.get("reccomplex.generationInfo.static.position.x"), IvTranslations.get("reccomplex.generationInfo.static.position.z")));

        addManagedSegment(3, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourcePlacer(generationInfo.placer, delegate, navigator))
                .buildDataSource(IvTranslations.get("reccomplex.placer"), IvTranslations.getLines("reccomplex.placer.tooltip")));

        addManagedSegment(4, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.gui.biomes"), generationInfo.dimensionMatcher, null));

        addManagedSegment(5, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceStaticPattern(generationInfo.pattern, delegate)
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
                cell.addPropertyConsumer(val -> generationInfo.relativeToSpawn = val);
                return new TableElementCell(IvTranslations.get("reccomplex.generationInfo.static.spawn"), cell);
            }
        }

        return super.elementForIndexInSegment(table, index, segment);
    }
}
