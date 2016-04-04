/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.maze.rules.saved;

import ivorius.ivtoolkit.maze.components.MazeComponent;
import ivorius.ivtoolkit.maze.components.MazePredicateMany;
import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.ivtoolkit.tools.NBTCompoundObjects;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.worldscripts.mazegenerator.rules.TableDataSourceMazeRuleConnectAll;
import ivorius.reccomplex.scripts.world.WorldScriptMazeGenerator;
import ivorius.reccomplex.structures.generic.maze.*;
import ivorius.reccomplex.structures.generic.maze.rules.LimitAABBStrategy;
import ivorius.reccomplex.structures.generic.maze.rules.MazeRule;
import ivorius.reccomplex.structures.generic.maze.rules.ReachabilityStrategy;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by lukas on 21.03.16.
 */
public class MazeRuleConnectAll extends MazeRule
{
    public final List<SavedMazePath> exits = new ArrayList<>();
    public boolean additive = false;

    public static Stream<SavedMazePath> getPaths(List<SavedMazePath> paths, List<SavedMazePathConnection> omega, Set<Connector> blockedConnections, ConnectorFactory connectorFactory)
    {
        return omega.stream().filter(e -> !blockedConnections.contains(e.connector.toConnector(connectorFactory))).map(e -> e.path).filter(e -> !paths.contains(e));
    }

    @Override
    public String displayString()
    {
        return !additive && exits.size() == 0 ? "Connect All" : "Connect " + (additive ? "" : "All - ") + exits.size();
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate, List<SavedMazePathConnection> expected, int[] boundsLower, int[] boundsHigher)
    {
        return new TableDataSourceMazeRuleConnectAll(this, delegate, navigator, expected, boundsLower, boundsHigher);
    }

    @Override
    public MazePredicateMany<MazeComponentStructure<Connector>, Connector> build(WorldScriptMazeGenerator script, Set<Connector> blockedConnections, ConnectorFactory connectorFactory, Collection<? extends MazeComponent<Connector>> components)
    {
        List<SavedMazePath> paths = additive ? exits : getPaths(exits, script.exitPaths, blockedConnections, connectorFactory).collect(Collectors.toList());

        if (paths.size() > 1)
        {
            Predicate<Connector> traverser = ReachabilityStrategy.connectorTraverser(blockedConnections);
            LimitAABBStrategy<MazeComponent<Object>, Object> limitStrategy = new LimitAABBStrategy<>(script.rooms.boundsSize());
            Set<Pair<MazeRoom, Set<MazeRoom>>> abilities = ReachabilityStrategy.compileAbilities(components, traverser);

            MazePredicateMany<MazeComponentStructure<Connector>, Connector> predicate = new MazePredicateMany<>();

            for (int i = 1; i < paths.size(); i++)
                predicate.predicates.add(new ReachabilityStrategy<>(
                        Collections.singleton(paths.get(i - 1).build()),
                        Collections.singleton(paths.get(i).build()),
                        traverser,
                        limitStrategy,
                        abilities
                ));

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
