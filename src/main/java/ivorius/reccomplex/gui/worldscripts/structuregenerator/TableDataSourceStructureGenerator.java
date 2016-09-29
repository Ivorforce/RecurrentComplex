/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.structuregenerator;

import ivorius.ivtoolkit.blocks.Directions;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.TableDataSourceBlockPos;
import ivorius.reccomplex.gui.TableDirections;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.scripts.world.WorldScriptStructureGenerator;
import ivorius.reccomplex.structures.StructureRegistry;
import joptsimple.internal.Strings;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

import static ivorius.reccomplex.gui.table.TableCellEnum.Option;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceStructureGenerator extends TableDataSourceSegmented
{
    protected WorldScriptStructureGenerator script;

    protected TableNavigator tableNavigator;
    protected TableDelegate tableDelegate;

    public TableDataSourceStructureGenerator(WorldScriptStructureGenerator script, TableNavigator tableNavigator, TableDelegate tableDelegate)
    {
        this.script = script;
        this.tableNavigator = tableNavigator;
        this.tableDelegate = tableDelegate;

        addManagedSegment(2, new TableDataSourceBlockPos(script.getStructureShift(), script::setStructureShift,
                new IntegerRange(-50, 50), new IntegerRange(-50, 50), new IntegerRange(-50, 50),
                IvTranslations.get("reccomplex.worldscript.strucGen.shift.x"), IvTranslations.get("reccomplex.worldscript.strucGen.shift.y"), IvTranslations.get("reccomplex.worldscript.strucGen.shift.z")));
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
        return script.isSimpleMode() ? 4 : 4;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        if (segment == 0)
            return 1;
        else if (segment == 1)
            return 1;
        else if (segment == 3)
            return script.isSimpleMode() ? 2 : 1;

        return super.sizeOfSegment(segment);
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            TableCellBoolean cell = new TableCellBoolean("simpleMode", script.isSimpleMode());
            cell.addPropertyConsumer(val -> {
                script.setSimpleMode(val);
                tableDelegate.reloadData();
            });
            return new TableElementCell(IvTranslations.get("reccomplex.worldscript.strucGen.mode.simple"), cell);
        }
        else if (segment == 1)
        {
            if (script.isSimpleMode())
            {
                TableCellString cell = new TableCellString("generators", Strings.join(script.getStructureNames(), ","));
                cell.setShowsValidityState(true);
                cell.setValidityState(doAllStructuresExist(script.getStructureNames()) ? GuiValidityStateIndicator.State.VALID : GuiValidityStateIndicator.State.SEMI_VALID);
                cell.addPropertyConsumer(val -> {
                    script.setStructureNames(Arrays.asList(val.split(",")));
                    cell.setValidityState(doAllStructuresExist(script.getStructureNames()) ? GuiValidityStateIndicator.State.VALID : GuiValidityStateIndicator.State.SEMI_VALID);
                });
                return new TableElementCell(IvTranslations.get("reccomplex.worldscript.strucGen.simple.generators"), cell)
                        .withTitleTooltip(IvTranslations.getLines("reccomplex.worldscript.strucGen.simple.generators.tooltip"));
            }
            else
            {
                TableCellString cell = new TableCellString("listID", script.getStructureListID());
                cell.addPropertyConsumer(script::setStructureListID);
                return new TableElementCell(IvTranslations.get("reccomplex.worldscript.strucGen.mode.list.id"), cell);
            }
        }
        else if (segment == 3)
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
                    cell.addPropertyConsumer(script::setStructureRotation);
                    return new TableElementCell(IvTranslations.get("reccomplex.rotation"), cell);
                }
                else if (index == 1)
                {
                    TableCellEnum<Boolean> cell = new TableCellEnum<>("mirror", script.getStructureMirror(),
                            new Option<>(false, IvTranslations.get("gui.false")),
                            new Option<>(true, IvTranslations.get("gui.true")),
                            new Option<>(null, IvTranslations.get("reccomplex.worldscript.strucGen.mirror.random")));
                    cell.addPropertyConsumer(script::setStructureMirror);
                    return new TableElementCell(IvTranslations.get("reccomplex.mirror"), cell);
                }
            }
            else
            {
                TableCellEnum<EnumFacing> cell = new TableCellEnum<>("front", script.getFront(), TableDirections.getDirectionOptions(ArrayUtils.add(Directions.HORIZONTAL, null), "random"));
                cell.addPropertyConsumer(script::setFront);
                return new TableElementCell(IvTranslations.get("reccomplex.worldscript.strucGen.mode.list.front"), cell);
            }
        }

        return super.elementForIndexInSegment(table, index, segment);
    }
}
