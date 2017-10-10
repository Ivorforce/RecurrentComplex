/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.structuregenerator;

import ivorius.ivtoolkit.blocks.Directions;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.TableDataSourceBlockPos;
import ivorius.reccomplex.gui.TableDirections;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.*;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.worldscripts.TableDataSourceWorldScript;
import ivorius.reccomplex.world.gen.script.WorldScriptStructureGenerator;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

import static ivorius.reccomplex.gui.table.cell.TableCellEnum.Option;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceWorldScriptStructureGenerator extends TableDataSourceSegmented
{
    protected WorldScriptStructureGenerator script;

    protected TableNavigator tableNavigator;
    protected TableDelegate tableDelegate;

    public TableDataSourceWorldScriptStructureGenerator(WorldScriptStructureGenerator script, TableNavigator tableNavigator, TableDelegate tableDelegate)
    {
        this.script = script;
        this.tableNavigator = tableNavigator;
        this.tableDelegate = tableDelegate;

        addManagedSegment(0, new TableDataSourceWorldScript(script));
        addManagedSegment(3, new TableDataSourceBlockPos(script.getStructureShift(), script::setStructureShift,
                IvTranslations.get("reccomplex.gui.blockpos.shift"), IvTranslations.getLines("reccomplex.gui.blockpos.shift.tooltip")));
    }

    private static boolean doAllStructuresExist(Iterable<String> structures)
    {
        for (String s : structures)
        {
            if (s.length() != 0 && StructureRegistry.INSTANCE.get(s) == null)
                return false; // s==0 = "No structure"
        }

        return true;
    }

    @Override
    public int numberOfSegments()
    {
        return script.isSimpleMode() ? 5 : 5;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        if (segment == 1)
            return 1;
        else if (segment == 2)
            return 1;
        else if (segment == 4)
            return script.isSimpleMode() ? 2 : 1;

        return super.sizeOfSegment(segment);
    }

    @Override
    public TableCell cellForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 1)
        {
            TableCellBoolean cell = new TableCellBoolean("simpleMode", script.isSimpleMode());
            cell.addListener(val -> {
                script.setSimpleMode(val);
                tableDelegate.reloadData();
            });
            return new TitledCell(IvTranslations.get("reccomplex.worldscript.strucGen.mode.simple"), cell);
        }
        else if (segment == 2)
        {
            if (script.isSimpleMode())
            {
                TableCellString cell = new TableCellString("generators", String.join(",", script.getStructureNames()));
                cell.setShowsValidityState(true);
                cell.setValidityState(doAllStructuresExist(script.getStructureNames()) ? GuiValidityStateIndicator.State.VALID : GuiValidityStateIndicator.State.SEMI_VALID);
                cell.addListener(val -> {
                    script.setStructureNames(Arrays.asList(val.split(",")));
                    cell.setValidityState(doAllStructuresExist(script.getStructureNames()) ? GuiValidityStateIndicator.State.VALID : GuiValidityStateIndicator.State.SEMI_VALID);
                });
                return new TitledCell(IvTranslations.get("reccomplex.worldscript.strucGen.simple.generators"), cell)
                        .withTitleTooltip(IvTranslations.getLines("reccomplex.worldscript.strucGen.simple.generators.tooltip"));
            }
            else
            {
                TableCellString cell = new TableCellString("listID", script.getStructureListID());
                cell.addListener(script::setStructureListID);
                return new TitledCell(IvTranslations.get("reccomplex.worldscript.strucGen.mode.list.id"), cell);
            }
        }
        else if (segment == 4)
        {
            if (script.isSimpleMode())
            {
                if (index == 0)
                {
                    TableCellEnum<Integer> cell = new TableCellEnum<>("rotation", script.getStructureRotation(),
                            new Option<>(0, IvTranslations.get("reccomplex.rotation.clockwise.0")),
                            new Option<>(1, IvTranslations.get("reccomplex.rotation.clockwise.1")),
                            new Option<>(2, IvTranslations.get("reccomplex.rotation.clockwise.2")),
                            new Option<>(3, IvTranslations.get("reccomplex.rotation.clockwise.3")),
                            new Option<>(null, IvTranslations.get("reccomplex.worldscript.strucGen.rotation.random")));
                    cell.addListener(script::setStructureRotation);
                    return new TitledCell(IvTranslations.get("reccomplex.rotation"), cell);
                }
                else if (index == 1)
                {
                    TableCellEnum<Boolean> cell = new TableCellEnum<>("mirror", script.getStructureMirror(),
                            new Option<>(false, IvTranslations.get("reccomplex.gui.false")),
                            new Option<>(true, IvTranslations.get("reccomplex.gui.true")),
                            new Option<>(null, IvTranslations.get("reccomplex.worldscript.strucGen.mirror.random")));
                    cell.addListener(script::setStructureMirror);
                    return new TitledCell(IvTranslations.get("reccomplex.mirror"), cell);
                }
            }
            else
            {
                TableCellEnum<EnumFacing> cell = new TableCellEnum<>("front", script.getFront(), TableDirections.getDirectionOptions(ArrayUtils.add(Directions.HORIZONTAL, null), "random"));
                cell.addListener(script::setFront);
                return new TitledCell(IvTranslations.get("reccomplex.worldscript.strucGen.mode.list.front"), cell);
            }
        }

        return super.cellForIndexInSegment(table, index, segment);
    }
}
