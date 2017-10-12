/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.client.rendering.MazeVisualizationContext;
import ivorius.reccomplex.gui.TableDataSourceBlockPos;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCellMultiBuilder;
import ivorius.reccomplex.gui.table.cell.TableCellString;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.worldscripts.TableDataSourceWorldScript;
import ivorius.reccomplex.gui.worldscripts.mazegenerator.rules.TableDataSourceMazeRuleList;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.MazeGeneration;
import ivorius.reccomplex.world.gen.script.WorldScriptMazeGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by lukas on 05.06.14.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceWorldScriptMazeGenerator extends TableDataSourceSegmented
{
    private WorldScriptMazeGenerator script;

    protected TableDelegate delegate;
    protected TableNavigator navigator;

    public TableDataSourceWorldScriptMazeGenerator(WorldScriptMazeGenerator script, BlockPos realWorldPos, TableDelegate delegate, TableNavigator navigator)
    {
        this.script = script;
        this.delegate = delegate;
        this.navigator = navigator;

        addSegment(0, new TableDataSourceWorldScript(script));

        addSegment(1, () -> TableCellMultiBuilder.create(navigator, delegate)
                .addCell(() -> {
                    TableCellString cell = new TableCellString("mazeID", script.getMazeID());
                    cell.setShowsValidityState(true);
                    cell.setValidityState(MazeGeneration.idValidity(cell.getPropertyValue()));
                    cell.addListener((mazeID) -> {
                        script.setMazeID(mazeID);
                        cell.setValidityState(MazeGeneration.idValidity(mazeID));
                    });
                    return cell;
                })
                .addNavigation(() -> new TableDataSourceMazeInfo(script, delegate, navigator))
                .buildTitled(IvTranslations.get("reccomplex.maze.id"))
        );

        addSegment(2, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceMazeComponent(script.mazeComponent, navigator, delegate)
                        .visualizing(new MazeVisualizationContext(script.structureShift.add(realWorldPos), script.roomSize))
                )
                .buildDataSource(IvTranslations.get("reccomplex.maze")));

        addSegment(3, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceMazeRuleList(script.rules, delegate, navigator, script.mazeComponent.exitPaths, script.mazeComponent.rooms))
                .buildDataSource(IvTranslations.get("reccomplex.worldscript.mazeGen.rules")));

        addSegment(4, new TableDataSourceBlockPos(script.getStructureShift(), script::setStructureShift,
                IvTranslations.get("reccomplex.gui.blockpos.shift"), IvTranslations.getLines("reccomplex.gui.blockpos.shift.tooltip")));
    }

    public WorldScriptMazeGenerator getScript()
    {
        return script;
    }

    public void setScript(WorldScriptMazeGenerator script)
    {
        this.script = script;
    }

    public TableDelegate getTableDelegate()
    {
        return delegate;
    }

    public void setTableDelegate(TableDelegate tableDelegate)
    {
        this.delegate = tableDelegate;
    }

    public TableNavigator getNavigator()
    {
        return navigator;
    }

    public void setNavigator(TableNavigator navigator)
    {
        this.navigator = navigator;
    }
}
