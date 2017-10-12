/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import ivorius.ivtoolkit.blocks.Directions;
import ivorius.ivtoolkit.gui.FloatRange;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.RCGuiTables;
import ivorius.reccomplex.gui.TableDataSourceBlockPos;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.TableDirections;
import ivorius.reccomplex.gui.table.TableCells;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCellEnum;
import ivorius.reccomplex.gui.table.cell.TableCellFloatRange;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.utils.scale.Scales;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.VanillaGeneration;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by lukas on 07.10.14.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceVanillaGeneration extends TableDataSourceSegmented
{
    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    private VanillaGeneration generationInfo;

    public TableDataSourceVanillaGeneration(TableNavigator navigator, TableDelegate tableDelegate, VanillaGeneration generationInfo)
    {
        this.navigator = navigator;
        this.tableDelegate = tableDelegate;
        this.generationInfo = generationInfo;

        addSegment(0, new TableDataSourceGenerationType(generationInfo, navigator, tableDelegate));

        addSegment(1, () -> {
            TableCellEnum<String> cell = new TableCellEnum<>("type", "village", new TableCellEnum.Option<String>("village", IvTranslations.get("reccomplex.generationInfo.vanilla.type.village")));
            return new TitledCell(IvTranslations.get("reccomplex.generationInfo.vanilla.type"), cell);
        });

        addSegment(2, () -> {
            return RCGuiTables.defaultWeightElement(val -> generationInfo.generationWeight = TableCells.toDouble(val), generationInfo.generationWeight);
        }, () -> {
            TableCellEnum<EnumFacing> cell = new TableCellEnum<>("front", generationInfo.front, TableDirections.getDirectionOptions(ArrayUtils.add(Directions.HORIZONTAL, null), "all"));
            cell.addListener(val -> generationInfo.front = val);
            return new TitledCell(IvTranslations.get("reccomplex.generationInfo.vanilla.front"), cell);
        });

        addSegment(3, () -> {
            TableCellFloatRange cell = new TableCellFloatRange("baseLimit", new FloatRange((float) generationInfo.minBaseLimit, (float) generationInfo.maxBaseLimit), 0, 1000, "%.2f");
            cell.setScale(Scales.pow(5));
            cell.addListener(val -> {
                generationInfo.minBaseLimit = val.getMin();
                generationInfo.maxBaseLimit = val.getMax();
            });
            return new TitledCell(IvTranslations.get("reccomplex.generationInfo.vanilla.amount.pervillage"), cell)
                    .withTitleTooltip(IvTranslations.getLines("reccomplex.generationInfo.vanilla.amount.pervillage.tooltip"));
        }, () -> {
            TableCellFloatRange cell = new TableCellFloatRange("scaledLimit", new FloatRange((float) generationInfo.minScaledLimit, (float) generationInfo.maxScaledLimit), 0, 1000, "%.2f");
            cell.setScale(Scales.pow(5));
            cell.addListener(val -> {
                generationInfo.minScaledLimit = val.getMin();
                generationInfo.maxScaledLimit = val.getMax();
            });
            return new TitledCell(IvTranslations.get("reccomplex.generationInfo.vanilla.amount.scaled"), cell)
                    .withTitleTooltip(IvTranslations.getLines("reccomplex.generationInfo.vanilla.amount.scaled.tooltip"));
        });

        addSegment(4, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.gui.biomes"), generationInfo.biomeExpression, null));
        addSegment(5, new TableDataSourceBlockPos(generationInfo.spawnShift, generationInfo::setSpawnShift,
                IvTranslations.get("reccomplex.gui.blockpos.shift"), IvTranslations.getLines("reccomplex.gui.blockpos.shift.tooltip")));
    }
}
