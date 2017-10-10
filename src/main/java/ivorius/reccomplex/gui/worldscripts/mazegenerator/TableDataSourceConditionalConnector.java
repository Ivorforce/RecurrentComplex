/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.SavedMazePathConnection;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 15.04.17.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceConditionalConnector extends TableDataSourceSegmented
{
    public SavedMazePathConnection.ConditionalConnector conditionalConnector;

    public TableDataSourceConditionalConnector(SavedMazePathConnection.ConditionalConnector conditionalConnector)
    {
        this.conditionalConnector = conditionalConnector;

        addManagedSegment(0, new TableDataSourceConnector(conditionalConnector.connector, IvTranslations.get("reccomplex.maze.connector")));
        addManagedSegment(1, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.maze.conditional_connector.condition"), conditionalConnector.expression, null));
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Conditional Connector";
    }

    @Override
    public int numberOfSegments()
    {
        return 2;
    }
}
