/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.maze.rules;

import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.maze.components.MazeComponent;
import ivorius.ivtoolkit.maze.components.MazePredicate;
import ivorius.ivtoolkit.tools.NBTCompoundObject;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.world.gen.script.WorldScriptMazeGenerator;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by lukas on 21.03.16.
 */
public abstract class MazeRule implements NBTCompoundObject
{
    public abstract String displayString();

    public abstract TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate, List<SavedMazePathConnection> expected, List<IntegerRange> bounds);

    public abstract MazePredicate<MazeComponentStructure<Connector>, Connector> build(WorldScriptMazeGenerator script, Set<Connector> blockedConnections, ConnectorFactory connectorFactory, Collection<? extends MazeComponent<Connector>> components);
}
