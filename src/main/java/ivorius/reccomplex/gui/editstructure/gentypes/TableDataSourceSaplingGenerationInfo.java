/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.RCGuiTables;
import ivorius.reccomplex.gui.TableDataSourceBlockPos;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.editstructure.pattern.TableDataSourceBlockPattern;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.world.gen.feature.structure.generic.gentypes.SaplingGenerationInfo;

/**
 * Created by lukas on 07.10.14.
 */
public class TableDataSourceSaplingGenerationInfo extends TableDataSourceSegmented
{
    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    private SaplingGenerationInfo generationInfo;

    public TableDataSourceSaplingGenerationInfo(TableNavigator navigator, TableDelegate tableDelegate, SaplingGenerationInfo generationInfo)
    {
        this.navigator = navigator;
        this.tableDelegate = tableDelegate;
        this.generationInfo = generationInfo;

        addManagedSegment(0, new TableDataSourceGenerationInfo(generationInfo, navigator, tableDelegate));
        addManagedSegment(2, new TableDataSourceBlockPos(generationInfo.spawnShift, generationInfo::setSpawnShift, new IntegerRange(-50, 50), new IntegerRange(-50, 50), new IntegerRange(-50, 50),
                IvTranslations.get("reccomplex.generationInfo.vanilla.shift.x"), IvTranslations.get("reccomplex.generationInfo.vanilla.shift.y"), IvTranslations.get("reccomplex.generationInfo.vanilla.shift.z")));
        addManagedSegment(3, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.gui.environment"), generationInfo.environmentMatcher, null));
        addManagedSegment(4, new TableDataSourceBlockPattern(generationInfo.pattern, tableDelegate, navigator));
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
        }
        return super.sizeOfSegment(segment);
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        switch (segment)
        {
            case 1:
            {
                return RCGuiTables.defaultWeightElement(val -> generationInfo.generationWeight = TableElements.toDouble(val), generationInfo.generationWeight);
            }
        }

        return super.elementForIndexInSegment(table, index, segment);
    }
}
