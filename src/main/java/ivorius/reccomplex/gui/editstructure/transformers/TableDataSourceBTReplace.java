/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.transformers;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.editstructure.TableDataSourceWeightedBlockStateList;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCellBoolean;
import ivorius.reccomplex.gui.table.cell.TableCellMultiBuilder;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.feature.structure.generic.transformers.TransformerReplace;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by lukas on 05.06.14.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceBTReplace extends TableDataSourceSegmented
{
    private TransformerReplace transformer;

    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    public TableDataSourceBTReplace(TransformerReplace transformer, TableNavigator navigator, TableDelegate delegate)
    {
        this.transformer = transformer;
        this.navigator = navigator;
        this.tableDelegate = delegate;

        addSegment(0, new TableDataSourceTransformer(transformer, delegate, navigator));
        addSegment(1, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.gui.sources"), IvTranslations.getLines("reccomplex.transformer.block.source.tooltip"), transformer.sourceMatcher, null));
        addSegment(2, () -> {
            TableCellBoolean cell = new TableCellBoolean(null, transformer.uniformly,
                    IvTranslations.get("reccomplex.transformer.replace.uniformity.true"),
                    IvTranslations.get("reccomplex.transformer.replace.uniformity.false")
            );
            cell.addListener(v -> transformer.uniformly = v);

            return new TitledCell("Uniformity", cell)
                    .withTitleTooltip(IvTranslations.getLines("reccomplex.transformer.replace.uniformity.tooltip"));
        });
        addSegment(3, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceWeightedBlockStateList(transformer.destination, delegate, navigator))
                .withTitle(IvTranslations.get("reccomplex.transformer.replace.destinations"))
                .buildDataSource());
    }

    public TransformerReplace getTransformer()
    {
        return transformer;
    }

    public void setTransformer(TransformerReplace transformer)
    {
        this.transformer = transformer;
    }
}
