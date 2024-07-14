/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes.staticgen;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.cell.*;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.StaticGeneration;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by lukas on 05.06.14.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceStaticPattern extends TableDataSourceSegmented
{
    private StaticGeneration.Pattern pattern;

    private TableDelegate tableDelegate;

    public TableDataSourceStaticPattern(StaticGeneration.Pattern pattern, TableDelegate tableDelegate)
    {
        this.pattern = pattern;
        this.tableDelegate = tableDelegate;

        addSegment(0, () -> {
            TableCellIntTextField cell = new TableCellIntTextField("repeatX", pattern.repeatX);
            cell.addListener(val -> pattern.repeatX = val);
            return new TitledCell(IvTranslations.get("reccomplex.generationInfo.static.pattern.repeat.x"), cell)
                    .withTitleTooltip(IvTranslations.getLines("reccomplex.generationInfo.static.pattern.repeat.tooltip"));
        }, () -> {
            TableCellIntTextField cell = new TableCellIntTextField("repeatZ", pattern.repeatZ);
            cell.addListener(val -> pattern.repeatZ = val);
            return new TitledCell(IvTranslations.get("reccomplex.generationInfo.static.pattern.repeat.z"), cell)
                    .withTitleTooltip(IvTranslations.getLines("reccomplex.generationInfo.static.pattern.repeat.tooltip"));
        });

        addSegment(1, () -> {
            TableCellIntSlider cell = new TableCellIntSlider("shiftX", pattern.randomShiftX, 0, 256);
            cell.addListener(val -> pattern.randomShiftX = val);
            return new TitledCell(IvTranslations.get("reccomplex.generationInfo.static.pattern.rshift.x"), cell)
                    .withTitleTooltip(IvTranslations.getLines("reccomplex.generationInfo.static.pattern.rshift.tooltip"));
        }, () -> {
            TableCellIntSlider cell = new TableCellIntSlider("shiftZ", pattern.randomShiftZ, 0, 256);
            cell.addListener(val -> pattern.randomShiftZ = val);
            return new TitledCell(IvTranslations.get("reccomplex.generationInfo.static.pattern.rshift.z"), cell)
                    .withTitleTooltip(IvTranslations.getLines("reccomplex.generationInfo.static.pattern.rshift.tooltip"));
        });

        addSegment(2, () -> {
            TableCellTextField cell = new TableCellString("biomeList", pattern.biomeList);
            cell.addListener(val -> pattern.biomeList = (String)val);
            return new TitledCell(IvTranslations.get("reccomplex.generationInfo.static.pattern.biomes"), cell)
                    .withTitleTooltip(IvTranslations.getLines("reccomplex.generationInfo.static.pattern.biomes.tooltip"));
        });

        addSegment(3, () -> {
            TableCellBoolean cell = new TableCellBoolean("whitelist", pattern.white);
            cell.addListener(val -> pattern.white = val);
            return new TitledCell(IvTranslations.get("reccomplex.generationInfo.static.pattern.biomes.whitelist"), cell)
                    .withTitleTooltip(IvTranslations.getLines("reccomplex.generationInfo.static.pattern.biomes.whitelist.tooltip"));
        });
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Static Pattern";
    }
}
