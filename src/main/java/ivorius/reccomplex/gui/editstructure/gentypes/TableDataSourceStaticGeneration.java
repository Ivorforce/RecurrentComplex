/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.TableDataSourceBlockSurfacePos;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.editstructure.gentypes.staticgen.TableDataSourceStaticPattern;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCellBoolean;
import ivorius.reccomplex.gui.table.cell.TableCellMultiBuilder;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.StaticGeneration;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by lukas on 07.10.14.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceStaticGeneration extends TableDataSourceSegmented
{
    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    private StaticGeneration generationInfo;

    public TableDataSourceStaticGeneration(TableNavigator navigator, TableDelegate delegate, StaticGeneration generationInfo)
    {
        this.navigator = navigator;
        this.tableDelegate = delegate;
        this.generationInfo = generationInfo;

        addSegment(0, new TableDataSourceGenerationType(generationInfo, navigator, delegate));

        addSegment(1, () -> {
            TableCellBoolean cell = new TableCellBoolean("relativeToSpawn", generationInfo.relativeToSpawn);
            cell.addListener(val -> generationInfo.relativeToSpawn = val);
            return new TitledCell(IvTranslations.get("reccomplex.generationInfo.static.spawn"), cell);
        });

        addSegment(2, new TableDataSourceBlockSurfacePos(generationInfo.position, generationInfo::setPosition));

        addSegment(3, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceSelectivePlacer(generationInfo.placer, navigator, delegate))
                .buildDataSource(IvTranslations.get("reccomplex.placer")));

        addSegment(4, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.gui.dimensions"), generationInfo.dimensionExpression, null));

        addSegment(5, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceStaticPattern(generationInfo.pattern, delegate))
                .enabled(generationInfo::hasPattern)
                .addAction(() -> generationInfo.pattern = generationInfo.hasPattern() ? null : new StaticGeneration.Pattern(), () -> generationInfo.hasPattern() ? IvTranslations.get("reccomplex.gui.remove") : IvTranslations.get("reccomplex.gui.add"), null
                ).buildDataSource(IvTranslations.get("reccomplex.generationInfo.static.pattern")));
    }
}
