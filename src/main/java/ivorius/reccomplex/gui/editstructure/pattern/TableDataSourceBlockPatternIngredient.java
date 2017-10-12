/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.pattern;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.cell.TableCellBoolean;
import ivorius.reccomplex.gui.table.cell.TableCellString;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.feature.structure.generic.BlockPattern;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 05.06.14.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceBlockPatternIngredient extends TableDataSourceSegmented
{
    private BlockPattern.Ingredient ingredient;

    private TableDelegate tableDelegate;

    public TableDataSourceBlockPatternIngredient(BlockPattern.Ingredient ingredient, TableDelegate tableDelegate)
    {
        this.ingredient = ingredient;
        this.tableDelegate = tableDelegate;

        addSegment(0, () -> {
            TableCellString cell = new TableCellString("", ingredient.identifier);
            cell.addListener(s -> ingredient.identifier = s);
            return new TitledCell(IvTranslations.get("reccomplex.blockpattern.ingredient.identifier"), cell);
        });

        addSegment(1, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.gui.blocks"), ingredient.matcher, null));

        addSegment(2, () -> {
            TableCellBoolean cell = new TableCellBoolean("", ingredient.delete);
            cell.addListener(d -> ingredient.delete = d);
            return new TitledCell(IvTranslations.get("reccomplex.blockpattern.ingredient.delete"), cell);
        });
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Ingredient";
    }
}
