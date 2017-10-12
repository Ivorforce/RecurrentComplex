/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.placer;

import ivorius.reccomplex.files.RCFileSaver;
import ivorius.reccomplex.gui.editstructure.preset.TableDataSourcePresettedObject;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.utils.presets.PresettedObject;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.GenericPlacer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 04.04.15.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourcePlacer extends TableDataSourceSegmented
{
    public TableDataSourcePlacer(PresettedObject<GenericPlacer> object, TableDelegate delegate, TableNavigator navigator)
    {
        addSegment(0, new TableDataSourcePresettedObject<>(object, RCFileSaver.PLACER_PRESET, delegate, navigator)
                .withApplyPresetAction(() -> addPresetSegments(object, delegate, navigator)));

        addPresetSegments(object, delegate, navigator);
    }

    public void addPresetSegments(final PresettedObject<GenericPlacer> object, final TableDelegate delegate, final TableNavigator navigator)
    {
        addSegment(1, new TableDataSourcePlacerFactorList(object.getContents().factors, delegate, navigator)
        {
            @Override
            public boolean canEditList()
            {
                return object.isCustom();
            }
        });
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Placer";
    }
}
