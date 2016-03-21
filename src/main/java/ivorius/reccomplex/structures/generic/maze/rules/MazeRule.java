/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.maze.rules;

import ivorius.ivtoolkit.maze.components.MazePredicate;
import ivorius.ivtoolkit.tools.NBTCompoundObject;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.scripts.world.WorldScriptMazeGenerator;
import ivorius.reccomplex.structures.generic.maze.Connector;
import ivorius.reccomplex.structures.generic.maze.MazeComponentStructure;
import ivorius.reccomplex.structures.generic.maze.SavedMazeComponent;

import java.util.Set;

/**
 * Created by lukas on 21.03.16.
 */
public abstract class MazeRule<C extends MazePredicate<MazeComponentStructure<Connector>, Connector>> implements NBTCompoundObject
{
    public abstract String displayString();

    public abstract TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate, int[] boundsLower, int[] boundsHigher);

    public abstract C build(WorldScriptMazeGenerator script, Set<Connector> blockedConnections);
}
