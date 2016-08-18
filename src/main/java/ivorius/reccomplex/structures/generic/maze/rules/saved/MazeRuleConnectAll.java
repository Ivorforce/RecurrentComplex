/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.maze.rules.saved;

import ivorius.ivtoolkit.maze.components.*;
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
import com.mojang.realmsclient.gui.ChatFormatting;

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

    public boolean preventConnection = false;

    public static Stream<SavedMazePath> getPaths(List<SavedMazePath> paths, List<SavedMazePathConnection> omega, Set<Connector> blockedConnections, ConnectorFactory connectorFactory)
    {
        return omega.stream().filter(e -> !blockedConnections.contains(e.connector.toConnector(connectorFactory))).map(e -> e.path).filter(e -> !paths.contains(e));
    }

    @Override
    public String displayString()
    {
        return String.format("%s%s %s", preventConnection ? ChatFormatting.GOLD + "Split" : ChatFormatting.GREEN + "Connect", ChatFormatting.RESET, !additive && exits.size() == 0 ? "All" : "Some");
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate, List<SavedMazePathConnection> expected, int[] boundsLower, int[] boundsHigher)
    {
        return new TableDataSourceMazeRuleConnectAll(this, delegate, navigator, expected, boundsLower, boundsHigher);
    }

    @Override
    public MazePredicate<MazeComponentStructure<Connector>, Connector> build(WorldScriptMazeGenerator script, Set<Connector> blockedConnections, ConnectorFactory connectorFactory, Collection<? extends MazeComponent<Connector>> components)
    {
        List<SavedMazePath> paths = additive ? exits : getPaths(exits, script.exitPaths, blockedConnections, connectorFactory).collect(Collectors.toList());

        if (paths.size() > 1)
        {
            List<Collection<MazePassage>> points = paths.stream().map(SavedMazePath::build).map(Collections::singleton).collect(Collectors.toList());
            Predicate<Connector> traverser = ReachabilityStrategy.connectorTraverser(blockedConnections);
            Predicate<MazeRoom> confiner = new LimitAABBStrategy<>(script.rooms.boundsSize());

            return preventConnection ? ReachabilityStrategy.preventConnection(points, traverser, confiner)
                    : ReachabilityStrategy.connect(points, traverser, confiner, ReachabilityStrategy.compileAbilities(components, traverser)
            );
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

        preventConnection = compound.getBoolean("preventConnection");
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        compound.setBoolean("additive", additive);
        NBTCompoundObjects.writeListTo(compound, "exits", exits);

        compound.setBoolean("preventConnection", preventConnection);
    }
}
