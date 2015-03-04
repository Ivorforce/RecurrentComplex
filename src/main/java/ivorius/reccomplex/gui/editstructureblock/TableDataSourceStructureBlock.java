/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructureblock;

import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.reccomplex.blocks.TileEntityStructureGenerator;
import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.utils.Directions;
import joptsimple.internal.Strings;

import java.util.Arrays;

import static ivorius.reccomplex.gui.table.TableElementList.Option;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceStructureBlock extends TableDataSourceSegmented implements TableElementPropertyListener
{
    protected TileEntityStructureGenerator structureGenerator;

    protected TableNavigator tableNavigator;
    protected TableDelegate tableDelegate;

    public TableDataSourceStructureBlock(TileEntityStructureGenerator structureGenerator, TableNavigator tableNavigator, TableDelegate tableDelegate)
    {
        this.structureGenerator = structureGenerator;
        this.tableNavigator = tableNavigator;
        this.tableDelegate = tableDelegate;
    }

    private static boolean doAllStructuresExist(Iterable<String> structures)
    {
        for (String s : structures)
        {
            if (s.length() != 0 && StructureRegistry.getStructure(s) == null)
                return false; // s==0 = "No structure"
        }

        return true;
    }

    public TileEntityStructureGenerator getStructureGenerator()
    {
        return structureGenerator;
    }

    public void setStructureGenerator(TileEntityStructureGenerator structureGenerator)
    {
        this.structureGenerator = structureGenerator;
    }

    @Override
    public int numberOfSegments()
    {
        return structureGenerator.isSimpleMode() ? 4 : 4;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        if (segment == 0)
            return 1;
        else if (segment == 1)
            return 1;
        else if (segment == 2)
            return 3;
        else if (segment == 3)
            return structureGenerator.isSimpleMode() ? 2 : 1;

        return 0;
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            TableElementBoolean element = new TableElementBoolean("simpleMode", "Simple Mode", structureGenerator.isSimpleMode());
            element.addPropertyListener(this);
            return element;
        }
        else if (segment == 1)
        {
            if (structureGenerator.isSimpleMode())
            {
                TableElementString element = new TableElementString("generators", "Generators (A,B,...)", Strings.join(structureGenerator.getStructureNames(), ","));
                element.setShowsValidityState(true);
                element.setValidityState(doAllStructuresExist(structureGenerator.getStructureNames()) ? GuiValidityStateIndicator.State.VALID : GuiValidityStateIndicator.State.SEMI_VALID);
                element.addPropertyListener(this);
                return element;
            }
            else
            {
                TableElementString element = new TableElementString("listID", "List ID", structureGenerator.getStructureListID());
                element.addPropertyListener(this);
                return element;
            }
        }
        else if (segment == 2)
        {
            if (index == 0)
            {
                TableElementInteger element = new TableElementInteger("xShift", "Shift: X", structureGenerator.getStructureShift().x, -50, 50);
                element.addPropertyListener(this);
                return element;
            }
            else if (index == 1)
            {
                TableElementInteger element = new TableElementInteger("yShift", "Shift: Y", structureGenerator.getStructureShift().y, -50, 50);
                element.addPropertyListener(this);
                return element;
            }
            else if (index == 2)
            {
                TableElementInteger element = new TableElementInteger("zShift", "Shift: Z", structureGenerator.getStructureShift().z, -50, 50);
                element.addPropertyListener(this);
                return element;
            }
        }
        else if (segment == 3)
        {
            if (structureGenerator.isSimpleMode())
            {
                if (index == 0)
                {
                    TableElementList element = new TableElementList("rotation", "Rotation", "" + structureGenerator.getStructureRotation(),
                            new Option("0", "0 Clockwise"), new Option("1", "1 Clockwise"), new Option("2", "2 Clockwise"), new Option("3", "3 Clockwise"), new Option("null", "Random (if rotatable)"));
                    element.addPropertyListener(this);
                    return element;
                }
                else if (index == 1)
                {
                    TableElementList element = new TableElementList("mirror", "Mirror", "" + structureGenerator.getStructureMirror(),
                            new Option("false", "false"), new Option("true", "true"), new Option("null", "Random (if mirrorable)"));
                    element.addPropertyListener(this);
                    return element;
                }
            }
            else
            {
                TableElementList.Option[] options = new TableElementList.Option[Directions.HORIZONTAL.length + 1];
                for (int i = 0; i < Directions.HORIZONTAL.length; i++)
                    options[i] = new TableElementList.Option(Directions.serialize(Directions.HORIZONTAL[i]), Directions.displayName(Directions.HORIZONTAL[i]));
                options[Directions.HORIZONTAL.length] = new TableElementList.Option("none", Directions.displayName(null));

                TableElementList element = new TableElementList("front", "Front",
                        structureGenerator.getFront() != null ? Directions.serialize(structureGenerator.getFront()) : "none", options);
                element.addPropertyListener(this);
                return element;
            }
        }

        return null;
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        switch (element.getID())
        {
            case "simpleMode":
                structureGenerator.setSimpleMode((Boolean) element.getPropertyValue());
                tableDelegate.reloadData();
                break;
            case "generators":
            {
                String value = ((String) element.getPropertyValue());
                structureGenerator.setStructureNames(Arrays.asList(value.split(",")));
                ((TableElementString) element).setValidityState(doAllStructuresExist(structureGenerator.getStructureNames()) ? GuiValidityStateIndicator.State.VALID : GuiValidityStateIndicator.State.SEMI_VALID);
                break;
            }
            case "listID":
            {
                structureGenerator.setStructureListID((String) element.getPropertyValue());
                break;
            }
            case "xShift":
            {
                BlockCoord shift = structureGenerator.getStructureShift();
                structureGenerator.setStructureShift(new BlockCoord((int) element.getPropertyValue(), shift.y, shift.z));
                break;
            }
            case "yShift":
            {
                BlockCoord shift = structureGenerator.getStructureShift();
                structureGenerator.setStructureShift(new BlockCoord(shift.x, (int) element.getPropertyValue(), shift.z));
                break;
            }
            case "zShift":
            {
                BlockCoord shift = structureGenerator.getStructureShift();
                structureGenerator.setStructureShift(new BlockCoord(shift.x, shift.y, (int) element.getPropertyValue()));
                break;
            }
            case "rotation":
            {
                String propertyID = (String) element.getPropertyValue();
                Integer rotation = propertyID.equals("null") ? null : Integer.valueOf(propertyID);
                structureGenerator.setStructureRotation(rotation);
                break;
            }
            case "mirror":
            {
                String propertyID = (String) element.getPropertyValue();
                Boolean mirror = propertyID.equals("null") ? null : Boolean.valueOf(propertyID);
                structureGenerator.setStructureMirror(mirror);
                break;
            }
            case "front":
            {
                String val = (String) element.getPropertyValue();
                structureGenerator.setFront("none".equals(val) ? null : Directions.deserializeHorizontal(val));
                break;
            }
        }
    }
}
