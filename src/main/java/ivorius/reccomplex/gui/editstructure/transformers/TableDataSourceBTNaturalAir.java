/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.transformers;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.TableCells;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCellFloatSlider;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.utils.scale.Scales;
import ivorius.reccomplex.world.gen.feature.structure.generic.transformers.TransformerNaturalAir;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by lukas on 05.06.14.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceBTNaturalAir extends TableDataSourceSegmented
{
    private TransformerNaturalAir transformer;

    public TableDataSourceBTNaturalAir(TransformerNaturalAir transformer, TableNavigator navigator, TableDelegate delegate)
    {
        this.transformer = transformer;

        addSegment(0, new TableDataSourceTransformer(transformer, delegate, navigator));
        addSegment(1, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.gui.sources"), IvTranslations.getLines("reccomplex.transformer.block.source.tooltip"), transformer.sourceMatcher, null));
        addSegment(2, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.gui.destinations"), IvTranslations.getLines("reccomplex.transformer.block.dest.tooltip"), transformer.destMatcher, null));

        addSegment(3, () -> {
                    TableCellFloatSlider cell = new TableCellFloatSlider("naturalExpansionDistance", TableCells.toFloat(transformer.naturalExpansionDistance), 0, 40);
                    cell.setScale(Scales.pow(5));
                    cell.addListener(val -> transformer.naturalExpansionDistance = TableCells.toDouble(val));
                    return new TitledCell(IvTranslations.get("reccomplex.transformer.naturalAir.naturalExpansionDistance"), cell)
                            .withTitleTooltip(IvTranslations.formatLines("reccomplex.transformer.naturalAir.naturalExpansionDistance.tooltip"));
                }, () -> {
                    TableCellFloatSlider cell = new TableCellFloatSlider("naturalExpansionRandomization", TableCells.toFloat(transformer.naturalExpansionRandomization), 0, 40);
                    cell.setScale(Scales.pow(5));
                    cell.addListener(val -> transformer.naturalExpansionRandomization = TableCells.toDouble(val));
                    return new TitledCell(IvTranslations.get("reccomplex.transformer.naturalAir.naturalExpansionRandomization"), cell)
                            .withTitleTooltip(IvTranslations.formatLines("reccomplex.transformer.naturalAir.naturalExpansionRandomization.tooltip"));
                }
        );
    }

    public TransformerNaturalAir getTransformer()
    {
        return transformer;
    }

    public void setTransformer(TransformerNaturalAir transformer)
    {
        this.transformer = transformer;
    }
}
