/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.maze.rules.saved;

import ivorius.ivtoolkit.maze.components.MazePredicateMany;
import ivorius.ivtoolkit.tools.NBTCompoundObjects;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.worldscripts.mazegenerator.rules.TableDataSourceMazeRuleConnectAll;
import ivorius.reccomplex.scripts.world.WorldScriptMazeGenerator;
import ivorius.reccomplex.structures.generic.maze.Connector;
import ivorius.reccomplex.structures.generic.maze.ConnectorFactory;
import ivorius.reccomplex.structures.generic.maze.MazeComponentStructure;
import ivorius.reccomplex.structures.generic.maze.SavedMazePath;
import ivorius.reccomplex.structures.generic.maze.rules.LimitAABBStrategy;
import ivorius.reccomplex.structures.generic.maze.rules.MazeRule;
import ivorius.reccomplex.structures.generic.maze.rules.ReachabilityStrategy;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by lukas on 21.03.16.
 */
public class MazeRuleConnectAll extends MazeRule
{
    public boolean additive = false;
    public final List<SavedMazePath> exits = new ArrayList<>();

    @Override
    public String displayString()
    {
        return !additive && exits.size() == 0 ? "Connect All" : "Connect " + (additive ? "" : "All - ") + exits.size();
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate, int[] boundsLower, int[] boundsHigher)
    {
        return new TableDataSourceMazeRuleConnectAll(this, delegate, navigator, boundsLower, boundsHigher);
    }

    @Override
    public MazePredicateMany<MazeComponentStructure<Connector>, Connector> build(WorldScriptMazeGenerator script, Set<Connector> blockedConnections, ConnectorFactory connectorFactory)
    {
        List<SavedMazePath> paths = additive ? exits : script.mazeExits.stream().filter(e -> !blockedConnections.contains(e.connector.toConnector(connectorFactory))).map(e -> e.path).filter(e -> !exits.contains(e)).collect(Collectors.toList());

        if (paths.size() > 1)
        {
            MazePredicateMany<MazeComponentStructure<Connector>, Connector> predicate = new MazePredicateMany<>();

            for (int i = 1; i < paths.size(); i++)
                predicate.predicates.add(new ReachabilityStrategy<>(Collections.singleton(paths.get(i - 1).toRoomConnection()), Collections.singleton(paths.get(i).toRoomConnection()), ReachabilityStrategy.connectorTraverser(blockedConnections), new LimitAABBStrategy<>(script.mazeRooms.boundsSize())));

            return predicate;
        }
        else
            return null;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        additive = compound.getBoolean("additive");
        exits.clear();
        exits.addAll(NBTCompoundObjects.readListFrom(compound, "exits", SavedMazePath.class));
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        compound.setBoolean("additive", additive);
        NBTCompoundObjects.writeListTo(compound, "exits", exits);
    }
}
