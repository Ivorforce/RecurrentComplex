/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.client.rendering.MazeVisualizationContext;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCellMultiBuilder;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.feature.structure.generic.Selection;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.SavedMazePathConnection;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 22.06.14.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceMazePathConnection extends TableDataSourceSegmented
{
    public TableDataSourceMazePathConnection(SavedMazePathConnection mazePath, Selection bounds, MazeVisualizationContext visualizationContext, TableDelegate tableDelegate, TableNavigator navigator)
    {
        addSegment(0, new TableDataSourceConnector(mazePath.connector, IvTranslations.get("reccomplex.maze.connector")));

        TableCellMultiBuilder tableCellMultiBuilder = TableCellMultiBuilder.create(navigator, tableDelegate)
                .addNavigation(() -> new TableDataSourceMazePathConditionalConnectorList(mazePath.conditionalConnectors, tableDelegate, navigator));
        addSegment(1, tableCellMultiBuilder.withTitle(IvTranslations.get("reccomplex.maze.conditional_connectors"), IvTranslations.getLines("reccomplex.maze.conditional_connectors.tooltip")).buildDataSource());

        addSegment(2, new TableDataSourceMazePath(mazePath.path, bounds, tableDelegate, navigator)
                .visualizing(visualizationContext));
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Path";
    }
}
