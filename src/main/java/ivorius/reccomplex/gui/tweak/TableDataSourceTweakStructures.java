/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.tweak;

import gnu.trove.map.TObjectFloatMap;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.cell.TableCellString;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 04.06.14.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceTweakStructures extends TableDataSourceSegmented
{
    protected TObjectFloatMap<String> tweaks;
    protected String search = "";

    public TableDataSourceTweakStructures(TableDelegate delegate, TObjectFloatMap<String> tweaks)
    {
        this.tweaks = tweaks;

        TableDataSourceTweakStructuresList tdsList = new TableDataSourceTweakStructuresList(delegate, tweaks);

        addSegment(0, () -> {
            TableCellString cell = new TableCellString(null, search);
            cell.addListener(s -> {
                delegate.setLocked("search", true);

                search = s;
                tdsList.search(s);

                delegate.setLocked("search", false);
            });

            return new TitledCell("search", "Search", cell);
        });

        addSegment(1, tdsList);
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Tweak Structure Spawn Rates";
    }
}
