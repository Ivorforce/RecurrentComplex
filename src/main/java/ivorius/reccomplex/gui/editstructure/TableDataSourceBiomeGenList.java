/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.files.RCFileSaver;
import ivorius.reccomplex.gui.editstructure.preset.TableDataSourcePresettedList;
import ivorius.reccomplex.gui.editstructure.preset.TableDataSourcePresettedObject;
import ivorius.reccomplex.gui.table.TableCells;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.world.gen.feature.structure.generic.BiomeGenerationInfo;
import ivorius.reccomplex.utils.presets.PresettedList;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceBiomeGenList extends TableDataSourceSegmented
{
    public TableDataSourceBiomeGenList(PresettedList<BiomeGenerationInfo> list, TableDelegate tableDelegate, TableNavigator navigator)
    {
        addManagedSegment(0, new TableDataSourcePresettedObject<>(list, RCFileSaver.BIOME_PRESET, tableDelegate, navigator)
                .withApplyPresetAction(() -> addPresetSegments(list, tableDelegate, navigator)));

        addPresetSegments(list, tableDelegate, navigator);
    }

    public void addPresetSegments(final PresettedList<BiomeGenerationInfo> list, final TableDelegate tableDelegate, final TableNavigator navigator)
    {
        addManagedSegment(1, new TableDataSourcePresettedList<BiomeGenerationInfo>(list, tableDelegate, navigator)
        {
            @Override
            public String getDisplayString(BiomeGenerationInfo biomeGenerationInfo)
            {
                return String.format("%s%s: %.2f", StringUtils.abbreviate(biomeGenerationInfo.getDisplayString(), 20), TextFormatting.RESET, biomeGenerationInfo.getActiveGenerationWeight());
            }

            @Override
            public BiomeGenerationInfo newEntry(String actionID)
            {
                return new BiomeGenerationInfo("", null);
            }

            @Nonnull
            @Override
            public TableCell entryCell(boolean enabled, BiomeGenerationInfo biomeGenerationInfo)
            {
                return TableCells.edit(enabled, navigator, tableDelegate, () -> new TableDataSourceBiomeGen(biomeGenerationInfo, tableDelegate));
            }
        });
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Biomes";
    }
}
