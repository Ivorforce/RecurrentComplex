/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.cell.TableCellEnum;
import ivorius.reccomplex.gui.table.cell.TableCellInteger;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.NaturalGeneration;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by lukas on 05.06.14.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceNaturalGenLimitation extends TableDataSourceSegmented
{
    private NaturalGeneration.SpawnLimitation limitation;

    private TableDelegate tableDelegate;

    public TableDataSourceNaturalGenLimitation(NaturalGeneration.SpawnLimitation limitation, TableDelegate tableDelegate)
    {
        this.limitation = limitation;
        this.tableDelegate = tableDelegate;

        addSegment(0, () -> {
            TableCellEnum<NaturalGeneration.SpawnLimitation.Context> cell = new TableCellEnum<>("context", limitation.context, TableCellEnum.options(NaturalGeneration.SpawnLimitation.Context.values(), "reccomplex.generationInfo.natural.limitation.context.", false));
            cell.addListener(val -> {
                limitation.context = val;
                tableDelegate.reloadData();
            });
            return new TitledCell("Context", cell);
        }, () -> {
            TableCellInteger cell = new TableCellInteger("max", limitation.maxCount, 1, 50);
            cell.addListener(val -> limitation.maxCount = val);
            return new TitledCell("Max Occurrences", cell);
        });

//        if (limitation.context == NaturalGenerationInfo.SpawnLimitation.Context.X_CHUNKS)
//        {
//            addSegment(1, () -> {
//                TableCellInteger cell = new TableCellInteger("chunks", limitation.chunkCount, 1, 100);
//                cell.addPropertyConsumer(this);
//                return new TableElementCell("Chunk Range", cell);
//            });
//        }
    }
}
