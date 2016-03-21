/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.maze.rules.saved;

import ivorius.ivtoolkit.maze.components.MazeRoomConnection;
import ivorius.ivtoolkit.tools.NBTCompoundObjects;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.structures.generic.maze.Connector;
import ivorius.reccomplex.structures.generic.maze.MazeComponentStructure;
import ivorius.reccomplex.structures.generic.maze.SavedMazeComponent;
import ivorius.reccomplex.structures.generic.maze.SavedMazePath;
import ivorius.reccomplex.structures.generic.maze.rules.LimitAABBStrategy;
import ivorius.reccomplex.structures.generic.maze.rules.MazeRule;
import ivorius.reccomplex.structures.generic.maze.rules.ReachabilityStrategy;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by lukas on 21.03.16.
 */
public class SavedReachabilityStrategy extends MazeRule<ReachabilityStrategy<MazeComponentStructure<Connector>, Connector>>
{
    public final List<SavedMazePath> start = new ArrayList<>();
    public final List<SavedMazePath> end = new ArrayList<>();

    protected static Set<MazeRoomConnection> buildPaths(List<SavedMazePath> start)
    {
        return start.stream().map(SavedMazePath::toRoomConnection).collect(Collectors.toSet());
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
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return null;
    }

    @Override
    public ReachabilityStrategy<MazeComponentStructure<Connector>, Connector> build(SavedMazeComponent component, Set<Connector> blockedConnections)
    {
        return new ReachabilityStrategy<>(buildPaths(start), buildPaths(end), ReachabilityStrategy.connectorTraverser(blockedConnections), new LimitAABBStrategy<>(component.getSize()));
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        NBTCompoundObjects.writeListTo(compound, "start", start);
        NBTCompoundObjects.writeListTo(compound, "end", end);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        start.addAll(NBTCompoundObjects.readListFrom(compound, "start", SavedMazePath.class));
        end.addAll(NBTCompoundObjects.readListFrom(compound, "end", SavedMazePath.class));
    }
}
