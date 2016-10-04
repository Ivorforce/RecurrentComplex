/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.placer;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCellFloat;
import ivorius.reccomplex.gui.table.cell.TableElementCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSupplied;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.FactorMatch;

/**
 * Created by lukas on 04.04.15.
 */
public class TableDataSourceFactorMatch extends TableDataSourceSegmented
{
    public FactorMatch placer;

    public TableDataSourceFactorMatch(FactorMatch factor, TableDelegate delegate, TableNavigator navigator)
    {
        this.placer = factor;

        addManagedSegment(0, new TableDataSourcePlacerFactor(factor, delegate, navigator));
        addManagedSegment(1, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.gui.sources"), factor.sourceMatcher, null));
        addManagedSegment(2, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.placer.factors.match.dest"), factor.destMatcher, null));
        addManagedSegment(3, new TableDataSourceSupplied(() -> {
            TableCellFloat cell = new TableCellFloat(null, factor.requiredConformity, 0, 1);
            cell.addPropertyConsumer(v -> factor.requiredConformity = v);
            return new TableElementCell(IvTranslations.get("reccomplex.placer.factors.match.conformity"), cell)
                    .withTitleTooltip(IvTranslations.getLines("reccomplex.placer.factors.match.conformity.tooltip"));
        }));
    }
}
