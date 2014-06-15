package ivorius.structuregen.gui.editstructureblock;

import ivorius.structuregen.blocks.TileEntityStructureGenerator;
import ivorius.structuregen.gui.table.*;
import joptsimple.internal.Strings;

import java.util.Arrays;

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
        return index >= 0 && index < 4;
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
            TableElementInteger element = new TableElementInteger("xShift", "Shift: X", structureGenerator.getStructureShiftX(), -50, 50);
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 2)
        {
            TableElementInteger element = new TableElementInteger("yShift", "Shift: Y", structureGenerator.getStructureShiftY(), -50, 50);
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 3)
        {
            TableElementInteger element = new TableElementInteger("zShift", "Shift: Z", structureGenerator.getStructureShiftZ(), -50, 50);
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
            structureGenerator.setStructureShiftX(((int) element.getPropertyValue()));
        }
        else if ("yShift".equals(element.getID()))
        {
            structureGenerator.setStructureShiftY(((int) element.getPropertyValue()));
        }
        else if ("zShift".equals(element.getID()))
        {
            structureGenerator.setStructureShiftZ(((int) element.getPropertyValue()));
        }
    }
}
