/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCellButton;
import ivorius.reccomplex.gui.table.cell.TableCellMulti;
import ivorius.reccomplex.gui.table.cell.TableCellString;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.feature.structure.Structures;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.GenerationType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 26.03.15.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceGenerationType extends TableDataSourceSegmented
{
    public GenerationType genInfo;

    public TableDelegate delegate;

    public TableDataSourceGenerationType(GenerationType genInfo, TableNavigator navigator, TableDelegate delegate)
    {
        this.genInfo = genInfo;
        this.delegate = delegate;

        addSegment(0, () -> {
            TableCellString idCell = new TableCellString("genInfoID", genInfo.id());
            idCell.setShowsValidityState(true);
            idCell.setValidityState(currentIDState());
            idCell.addListener(val ->
            {
                genInfo.setID(val);
                idCell.setValidityState(currentIDState());
            });

            TableCellButton randomizeCell = new TableCellButton(null, null, IvTranslations.get("reccomplex.gui.randomize.short"), IvTranslations.getLines("reccomplex.gui.randomize"));
            randomizeCell.addAction(() -> {
                genInfo.setID(GenerationType.randomID(genInfo.getClass()));
                delegate.reloadData();
            });

            TableCellMulti cell = new TableCellMulti(idCell, randomizeCell);
            cell.setSize(1, 0.1f);
            return new TitledCell(IvTranslations.get("reccomplex.structure.generation.id"), cell)
                    .withTitleTooltip(IvTranslations.formatLines("reccomplex.structure.generation.id.tooltip"));
        });
    }

    @Nonnull
    @Override
    public String title()
    {
        return genInfo.displayString();
    }

    protected GuiValidityStateIndicator.State currentIDState()
    {
        return Structures.isSimpleIDState(genInfo.id());
    }
}
