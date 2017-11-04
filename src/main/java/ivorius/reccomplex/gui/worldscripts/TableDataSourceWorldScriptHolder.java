/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.worldscripts;

import ivorius.ivtoolkit.blocks.BlockPositions;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.TableDataSourceBlockPos;
import ivorius.reccomplex.gui.TableDataSourceBlockState;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCellButton;
import ivorius.reccomplex.gui.table.cell.TableCellTitle;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.network.PacketWorldDataHandler;
import ivorius.reccomplex.world.gen.feature.structure.Structures;
import ivorius.reccomplex.world.gen.script.WorldScriptHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 05.06.14.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceWorldScriptHolder extends TableDataSourceSegmented
{
    public WorldScriptHolder script;

    protected TableNavigator tableNavigator;
    protected TableDelegate tableDelegate;

    protected BlockPos pos2;

    public TableDataSourceWorldScriptHolder(WorldScriptHolder script, BlockPos worldPos, TableNavigator tableNavigator, TableDelegate tableDelegate)
    {
        this.script = script;
        this.tableNavigator = tableNavigator;
        this.tableDelegate = tableDelegate;

        addSegment(0, () -> {
            if (script.worldData == null)
            {
                TableCellButton cell = new TableCellButton(null, null, IvTranslations.get("reccomplex.worldscript.holder.capture"));
                cell.addAction(() -> PacketWorldDataHandler.capture(script, worldPos, pos2));
                return cell;
            }
            else
            {
                TableCellButton cell = new TableCellButton(null, null, IvTranslations.get("reccomplex.worldscript.holder.delete"));
                cell.addAction(() -> {
                    script.worldData = null;
                    tableDelegate.reloadData();
                });
                return cell;
            }
        }, () -> {
            TableCellButton cell = new TableCellButton(null, null, IvTranslations.get("reccomplex.worldscript.holder.switch"));
            cell.addAction(() -> PacketWorldDataHandler.swap(script, worldPos));
            cell.setEnabled(script.worldData != null);
            return cell;
        });

        TableDataSourceBlockPos cellPoint1 = new TableDataSourceBlockPos(script.origin, p -> script.origin = p,
                IvTranslations.get("reccomplex.worldscript.holder.point.1"), IvTranslations.getLines("reccomplex.worldscript.holder.point.1.tooltip"));
        cellPoint1.setEnabled(() -> script.worldData == null);
        addSegment(1, cellPoint1);

        pos2 = script.worldData != null
                ? script.origin.add(BlockPositions.fromIntArray(Structures.size(script.worldData, null))).add(-1, -1, -1)
                : script.origin.add(0, 0, 0);

        TableDataSourceBlockPos cellPoint2 = new TableDataSourceBlockPos(pos2, p -> pos2 = p,
                IvTranslations.get("reccomplex.worldscript.holder.point.2"), IvTranslations.getLines("reccomplex.worldscript.holder.point.2.tooltip"));
        cellPoint2.setEnabled(() -> script.worldData == null);
        addSegment(2, cellPoint2);

        addSegment(3, () -> {
            return new TitledCell(new TableCellTitle(null, IvTranslations.get("reccomplex.worldscript.holder.self"))
                    .withTooltip(IvTranslations.getLines("reccomplex.worldscript.holder.self.tooltip")));
        });
        addSegment(4, new TableDataSourceBlockState(script.replaceState, s -> script.replaceState = s, tableNavigator, tableDelegate));
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Holder";
    }
}
