/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.RCGuiTables;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.BiomeGenerationInfo;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBiomeGen extends TableDataSourceSegmented
{
    private BiomeGenerationInfo generationInfo;

    private TableDelegate tableDelegate;

    public TableDataSourceBiomeGen(BiomeGenerationInfo generationInfo, TableDelegate tableDelegate)
    {
        this.generationInfo = generationInfo;
        this.tableDelegate = tableDelegate;

        addManagedSection(0, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.gui.biomes"), generationInfo.getBiomeMatcher()));
    }

    @Override
    public int numberOfSegments()
    {
        return 2;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return segment == 1 ? 1 : super.sizeOfSegment(segment);
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 1)
        {
            return RCGuiTables.defaultWeightElement(val -> generationInfo.setGenerationWeight(TableElements.toDouble(val)), generationInfo.getGenerationWeight());
        }

        return super.elementForIndexInSegment(table, index, segment);
    }
}
