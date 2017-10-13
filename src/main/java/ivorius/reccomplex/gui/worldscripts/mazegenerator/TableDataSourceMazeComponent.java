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
import ivorius.reccomplex.gui.worldscripts.mazegenerator.reachability.TableDataSourceMazeReachability;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.SavedMazeComponent;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.SavedMazeReachability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 26.04.15.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceMazeComponent extends TableDataSourceSegmented
{
    public static final int[] DEFAULT_MAX_COMPONENT_SIZE = {100, 100, 100};

    protected SavedMazeComponent component;

    protected TableNavigator navigator;
    protected TableDelegate tableDelegate;

    protected MazeVisualizationContext visualizationContext;

    public TableDataSourceMazeComponent(SavedMazeComponent component, TableNavigator navigator, TableDelegate tableDelegate)
    {
        this.component = component;
        this.navigator = navigator;
        this.tableDelegate = tableDelegate;

        addSegment(0, new TableDataSourceConnector(component.defaultConnector, IvTranslations.get("reccomplex.maze.connector.default")));

        TableCellMultiBuilder tableCellMultiBuilder2 = TableCellMultiBuilder.create(navigator, tableDelegate)
                .addNavigation(() -> new TableDataSourceSelection(component.rooms, DEFAULT_MAX_COMPONENT_SIZE, tableDelegate, navigator, false)
                        .visualizing(visualizationContext));
        addSegment(1, tableCellMultiBuilder2.withTitle(IvTranslations.get("reccomplex.generationInfo.mazeComponent.rooms"), IvTranslations.getLines("reccomplex.generationInfo.mazeComponent.rooms.tooltip")).buildDataSource());

        TableCellMultiBuilder tableCellMultiBuilder1 = TableCellMultiBuilder.create(navigator, tableDelegate)
                .addNavigation(() -> new TableDataSourceMazePathConnectionList(component.exitPaths, tableDelegate, navigator, component.rooms)
                        .visualizing(visualizationContext))
                .enabled(() -> component.rooms.size() > 0);
        addSegment(2, tableCellMultiBuilder1.withTitle(IvTranslations.get("reccomplex.generationInfo.mazeComponent.exits"), IvTranslations.getLines("reccomplex.generationInfo.mazeComponent.exits.tooltip")).buildDataSource());

        TableCellMultiBuilder tableCellMultiBuilder = TableCellMultiBuilder.create(navigator, tableDelegate)
                .addNavigation(() -> new TableDataSourceMazeReachability(component.reachability, visualizationContext, tableDelegate, navigator, SavedMazeReachability.buildExpected(component), component.rooms))
                .enabled(() -> component.rooms.size() > 0);
        addSegment(3, tableCellMultiBuilder.withTitle(IvTranslations.get("reccomplex.generationInfo.mazeComponent.reachability"), IvTranslations.formatLines("reccomplex.reachability.tooltip")).buildDataSource());

    }

    public TableDataSourceMazeComponent visualizing(MazeVisualizationContext visualizationContext)
    {
        this.visualizationContext = visualizationContext;
        return this;
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Maze Component";
    }
}
