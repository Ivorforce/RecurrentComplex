/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructureblock;

import ivorius.reccomplex.blocks.TileEntityStructureGenerator;
import ivorius.reccomplex.gui.table.*;
import ivorius.ivtoolkit.blocks.BlockCoord;
import joptsimple.internal.Strings;

import java.util.Arrays;

import static ivorius.reccomplex.gui.table.TableElementList.Option;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceStructureBlock implements TableDataSource, TableElementPropertyListener
{
    private TileEntityStructureGenerator structureGenerator;

    public TableDataSourceStructureBlock(TileEntityStructureGenerator structureGenerator)
    {
        this.structureGenerator = structureGenerator;
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
    public boolean has(GuiTable table, int index)
    {
        return index >= 0 && index < 6;
    }

    @Override
    public TableElement elementForIndex(GuiTable table, int index)
    {
        if (index == 0)
        {
            TableElementString element = new TableElementString("generators", "Generators (A,B,...)", Strings.join(structureGenerator.getStructureNames(), ","));
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 1)
        {
            TableElementInteger element = new TableElementInteger("xShift", "Shift: X", structureGenerator.getStructureShift().x, -50, 50);
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 2)
        {
            TableElementInteger element = new TableElementInteger("yShift", "Shift: Y", structureGenerator.getStructureShift().y, -50, 50);
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 3)
        {
            TableElementInteger element = new TableElementInteger("zShift", "Shift: Z", structureGenerator.getStructureShift().z, -50, 50);
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 4)
        {
            TableElementList element = new TableElementList("rotation", "Rotation", "" + structureGenerator.getStructureRotation(), new Option("0", "0 Clockwise"), new Option("1", "1 Clockwise"), new Option("2", "2 Clockwise"), new Option("3", "3 Clockwise"), new Option("null", "Random (if rotatable)"));
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 5)
        {
            TableElementList element = new TableElementList("mirror", "Mirror", "" + structureGenerator.getStructureMirror(), new Option("false", "false"), new Option("true", "true"), new Option("null", "Random (if mirrorable)"));
            element.addPropertyListener(this);
            return element;
        }

        return null;
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        if ("generators".equals(element.getID()))
        {
            String value = ((String) element.getPropertyValue());
            structureGenerator.setStructureNames(Arrays.asList(value.split(",")));
        }
        else if ("xShift".equals(element.getID()))
        {
            BlockCoord shift = structureGenerator.getStructureShift();
            structureGenerator.setStructureShift(new BlockCoord((int) element.getPropertyValue(), shift.y, shift.z));
        }
        else if ("yShift".equals(element.getID()))
        {
            BlockCoord shift = structureGenerator.getStructureShift();
            structureGenerator.setStructureShift(new BlockCoord(shift.x, (int) element.getPropertyValue(), shift.z));
        }
        else if ("zShift".equals(element.getID()))
        {
            BlockCoord shift = structureGenerator.getStructureShift();
            structureGenerator.setStructureShift(new BlockCoord(shift.x, shift.y, (int) element.getPropertyValue()));
        }
        else if ("rotation".equals(element.getID()))
        {
            String propertyID = (String) element.getPropertyValue();
            Integer rotation = propertyID.equals("null") ? null : Integer.valueOf(propertyID);
            structureGenerator.setStructureRotation(rotation);
        }
        else if ("mirror".equals(element.getID()))
        {
            String propertyID = (String) element.getPropertyValue();
            Boolean mirror = propertyID.equals("null") ? null : Boolean.valueOf(propertyID);
            structureGenerator.setStructureMirror(mirror);
        }
    }
}
