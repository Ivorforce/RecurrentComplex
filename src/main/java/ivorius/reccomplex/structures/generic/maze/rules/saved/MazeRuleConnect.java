/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.maze.rules.saved;

import ivorius.ivtoolkit.maze.components.MazeComponent;
import ivorius.ivtoolkit.maze.components.MazePassage;
import ivorius.ivtoolkit.maze.components.MazeRoomConnection;
import ivorius.ivtoolkit.tools.NBTCompoundObjects;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.worldscripts.mazegenerator.rules.TableDataSourceMazeRuleConnect;
import ivorius.reccomplex.scripts.world.WorldScriptMazeGenerator;
import ivorius.reccomplex.structures.generic.maze.*;
import ivorius.reccomplex.structures.generic.maze.rules.LimitAABBStrategy;
import ivorius.reccomplex.structures.generic.maze.rules.MazeRule;
import ivorius.reccomplex.structures.generic.maze.rules.ReachabilityStrategy;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by lukas on 21.03.16.
 */
public class MazeRuleConnect extends MazeRule
{
    public final List<SavedMazePath> start = new ArrayList<>();
    public final List<SavedMazePath> end = new ArrayList<>();

    protected static Set<MazePassage> buildPaths(List<SavedMazePath> start)
    {
        return start.stream().map(SavedMazePath::build).collect(Collectors.toSet());
    }

    @Override
    public String displayString()
    {
        return String.format("%s -> %s", summarize(start), summarize(end));
    }

    private String summarize(List<SavedMazePath> list)
    {
        return list.size() == 0 ? "?" : String.format("%s%s", list.get(0).getSourceRoom().toString(), list.size() > 1 ? "..." : "");
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate, List<SavedMazePathConnection> expected, int[] boundsLower, int[] boundsHigher)
    {
        return new TableDataSourceMazeRuleConnect(this, delegate, navigator, boundsLower, boundsHigher);
    }

    @Override
    public ReachabilityStrategy<MazeComponentStructure<Connector>, Connector> build(WorldScriptMazeGenerator script, Set<Connector> blockedConnections, ConnectorFactory connectorFactory, Collection<? extends MazeComponent<Connector>> components)
    {
        if (start.size() > 0 && end.size() > 0)
        {
            Predicate<Connector> traverser = ReachabilityStrategy.connectorTraverser(blockedConnections);
            return new ReachabilityStrategy<>(
                    buildPaths(start),
                    buildPaths(end),
                    traverser,
                    new LimitAABBStrategy<>(script.rooms.boundsSize()),
                    ReachabilityStrategy.compileAbilities(components, traverser)
            );
        }
        else
            return null;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        start.clear();
        start.addAll(NBTCompoundObjects.readListFrom(compound, "start", SavedMazePath.class));

        end.clear();
        end.addAll(NBTCompoundObjects.readListFrom(compound, "end", SavedMazePath.class));
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        NBTCompoundObjects.writeListTo(compound, "start", start);
        NBTCompoundObjects.writeListTo(compound, "end", end);
    }
}
