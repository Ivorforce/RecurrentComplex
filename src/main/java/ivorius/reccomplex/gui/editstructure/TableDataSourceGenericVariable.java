/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.cell.TableCellBoolean;
import ivorius.reccomplex.gui.table.cell.TableCellFloatSlider;
import ivorius.reccomplex.gui.table.cell.TableCellString;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.utils.scale.Scales;
import ivorius.reccomplex.world.gen.feature.structure.Structures;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericVariableDomain;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 05.06.14.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceGenericVariable extends TableDataSourceSegmented
{
    private GenericVariableDomain.Variable variable;

    private TableDelegate tableDelegate;

    public TableDataSourceGenericVariable(GenericVariableDomain.Variable variable)
    {
        this.variable = variable;

        addSegment(0, () -> {
            TableCellString cell = new TableCellString(null, variable.id);
            cell.setShowsValidityState(true);
            cell.addListener(s ->
            {
                if (Structures.isSimpleID(s))
                {
                    cell.setValidityState(GuiValidityStateIndicator.State.VALID);
                    variable.id = s;
                }
                else
                    cell.setValidityState(GuiValidityStateIndicator.State.INVALID);
            });
            return new TitledCell(IvTranslations.get("reccomplex.structure.variables.id"), cell)
                    .withTitleTooltip(IvTranslations.getLines("reccomplex.structure.variables.id.tooltip"));
        }, () -> {
            TableCellFloatSlider cell = new TableCellFloatSlider(null, variable.chance, 0, 1);
            cell.setScale(Scales.pow(5));
            cell.addListener(f -> variable.chance = f);
            return new TitledCell(IvTranslations.get("reccomplex.structure.variables.chance"), cell)
                    .withTitleTooltip(IvTranslations.getLines("reccomplex.structure.variables.chance.tooltip"));
        }, () -> {
            TableCellBoolean cell = new TableCellBoolean(null, variable.affectsLogic);
            cell.addListener(b -> variable.affectsLogic = b);
            return new TitledCell(IvTranslations.get("reccomplex.structure.variables.logical"), cell)
                    .withTitleTooltip(IvTranslations.getLines("reccomplex.structure.variables.logical.tooltip"));
        });

        addSegment(1, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.structure.variables.condition"), variable.condition, null));
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Local Toggle";
    }
}
