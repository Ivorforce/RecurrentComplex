/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import ivorius.ivtoolkit.gui.FloatRange;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.reccomplex.gui.RCGuiTables;
import ivorius.reccomplex.gui.TableDataSourceBlockPos;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.TableDirections;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCellEnum;
import ivorius.reccomplex.gui.table.cell.TableCellFloatRange;
import ivorius.reccomplex.gui.table.cell.TableElementCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.feature.structure.generic.gentypes.VanillaGenerationInfo;
import ivorius.ivtoolkit.blocks.Directions;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.utils.scale.Scales;
import net.minecraft.util.EnumFacing;

/**
 * Created by lukas on 07.10.14.
 */
public class TableDataSourceVanillaStructureGenerationInfo extends TableDataSourceSegmented
{
    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    private VanillaGenerationInfo generationInfo;

    public TableDataSourceVanillaStructureGenerationInfo(TableNavigator navigator, TableDelegate tableDelegate, VanillaGenerationInfo generationInfo)
    {
        this.navigator = navigator;
        this.tableDelegate = tableDelegate;
        this.generationInfo = generationInfo;

        addManagedSegment(0, new TableDataSourceGenerationInfo(generationInfo, navigator, tableDelegate));
        addManagedSegment(4, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.gui.biomes"), generationInfo.biomeMatcher, null));
        addManagedSegment(5, new TableDataSourceBlockPos(generationInfo.spawnShift, generationInfo::setSpawnShift, null, null, null,
                IvTranslations.get("reccomplex.generationInfo.vanilla.shift.x"), IvTranslations.get("reccomplex.generationInfo.vanilla.shift.y"), IvTranslations.get("reccomplex.generationInfo.vanilla.shift.z")));
    }

    @Override
    public int numberOfSegments()
    {
        return 6;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 2;
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
                TableCellEnum<String> cell = new TableCellEnum<>("type", "village", new TableCellEnum.Option<>("village", IvTranslations.get("reccomplex.generationInfo.vanilla.type.village")));
                return new TableElementCell(IvTranslations.get("reccomplex.generationInfo.vanilla.type"), cell);
            }
            case 2:
            {
                switch (index)
                {
                    case 0:
                        return RCGuiTables.defaultWeightElement(val -> generationInfo.generationWeight = TableElements.toDouble(val), generationInfo.generationWeight);
                    case 1:
                    {
                        TableCellEnum<EnumFacing> cell = new TableCellEnum<>("front", generationInfo.front, TableDirections.getDirectionOptions(Directions.HORIZONTAL));
                        cell.addPropertyConsumer(val -> generationInfo.front = val);
                        return new TableElementCell(IvTranslations.get("reccomplex.generationInfo.vanilla.front"), cell);
                    }
                }
            }
            case 3:
                switch (index)
                {
                    case 0:
                    {
                        TableCellFloatRange cell = new TableCellFloatRange("baseLimit", new FloatRange((float) generationInfo.minBaseLimit, (float) generationInfo.maxBaseLimit), 0, 1000, "%.2f");
                        cell.setScale(Scales.pow(5));
                        cell.addPropertyConsumer(val -> {
                            generationInfo.minBaseLimit = val.getMin();
                            generationInfo.maxBaseLimit = val.getMax();
                        });
                        return new TableElementCell(IvTranslations.get("reccomplex.generationInfo.vanilla.amount.pervillage"), cell)
                                .withTitleTooltip(IvTranslations.getLines("reccomplex.generationInfo.vanilla.amount.pervillage.tooltip"));
                    }
                    case 1:
                    {
                        TableCellFloatRange cell = new TableCellFloatRange("scaledLimit", new FloatRange((float) generationInfo.minScaledLimit, (float) generationInfo.maxScaledLimit), 0, 1000, "%.2f");
                        cell.setScale(Scales.pow(5));
                        cell.addPropertyConsumer(val -> {
                            generationInfo.minScaledLimit = val.getMin();
                            generationInfo.maxScaledLimit = val.getMax();
                        });
                        return new TableElementCell(IvTranslations.get("reccomplex.generationInfo.vanilla.amount.scaled"), cell)
                                .withTitleTooltip(IvTranslations.getLines("reccomplex.generationInfo.vanilla.amount.scaled.tooltip"));
                    }
                }
                break;
        }

        return super.elementForIndexInSegment(table, index, segment);
    }
}
