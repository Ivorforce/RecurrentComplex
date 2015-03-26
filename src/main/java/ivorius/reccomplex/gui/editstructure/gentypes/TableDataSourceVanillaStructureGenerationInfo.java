/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.gentypes.VanillaStructureSpawnInfo;

/**
 * Created by lukas on 07.10.14.
 */
public class TableDataSourceVanillaStructureGenerationInfo extends TableDataSourceSegmented implements TableElementPropertyListener
{
    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    private VanillaStructureSpawnInfo generationInfo;

    public TableDataSourceVanillaStructureGenerationInfo(TableNavigator navigator, TableDelegate tableDelegate, VanillaStructureSpawnInfo generationInfo)
    {
        this.navigator = navigator;
        this.tableDelegate = tableDelegate;
        this.generationInfo = generationInfo;

        addManagedSection(0, new TableDataSourceGenerationInfo(generationInfo));
    }

    @Override
    public int numberOfSegments()
    {
        return 5;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
            case 1:
                return 1;
            case 2:
                return 1;
            case 3:
                return 4;
            case 4:
                return 3;
        }
        return super.sizeOfSegment(segment);
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        switch (segment)
        {
            case 1:
            {
                TableElementList element = new TableElementList("type", "Type", "village", new TableElementList.Option("village", "Village"));
                element.addPropertyListener(this);
                return element;
            }
            case 2:
            {
                TableElementInteger element = new TableElementInteger("spawnWeight", "Spawn Weight", generationInfo.spawnWeight, 1, 200);
                element.addPropertyListener(this);
                return element;
            }
            case 3:
                switch (index)
                {
                    case 0:
                    {
                        TableElementInteger element = new TableElementInteger("minBaseLimit", "Min. Base Limit", generationInfo.minBaseLimit, 0, 100);
                        element.addPropertyListener(this);
                        return element;
                    }
                    case 1:
                    {
                        TableElementInteger element = new TableElementInteger("maxBaseLimit", "Max. Base Limit", generationInfo.maxBaseLimit, 0, 100);
                        element.addPropertyListener(this);
                        return element;
                    }
                    case 2:
                    {
                        TableElementInteger element = new TableElementInteger("minScaleLimit", "Min. Scaled Limit", generationInfo.minScaledLimit, 0, 100);
                        element.addPropertyListener(this);
                        return element;
                    }
                    case 3:
                    {
                        TableElementInteger element = new TableElementInteger("maxScaleLimit", "Max. Scaled Limit", generationInfo.maxScaledLimit, 0, 100);
                        element.addPropertyListener(this);
                        return element;
                    }
                }
                break;
            case 4:
                switch (index)
                {
                    case 0:
                    {
                        TableElementInteger element = new TableElementInteger("spawnX", "Spawn Shift X", generationInfo.spawnShift.x, -50, 50);
                        element.addPropertyListener(this);
                        return element;
                    }
                    case 1:
                    {
                        TableElementInteger element = new TableElementInteger("spawnY", "Spawn Shift Y", generationInfo.spawnShift.y, -50, 50);
                        element.addPropertyListener(this);
                        return element;
                    }
                    case 2:
                    {
                        TableElementInteger element = new TableElementInteger("spawnZ", "Spawn Shift Z", generationInfo.spawnShift.z, -50, 50);
                        element.addPropertyListener(this);
                        return element;
                    }
                }
                break;
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        if ("spawnWeight".equals(element.getID()))
        {
            generationInfo.spawnWeight = (int) element.getPropertyValue();
        }
        else if ("minBaseLimit".equals(element.getID()))
        {
            generationInfo.minBaseLimit = (int) element.getPropertyValue();
        }
        else if ("maxBaseLimit".equals(element.getID()))
        {
            generationInfo.maxBaseLimit = (int) element.getPropertyValue();
        }
        else if ("minScaledLimit".equals(element.getID()))
        {
            generationInfo.minScaledLimit = (int) element.getPropertyValue();
        }
        else if ("maxScaledLimit".equals(element.getID()))
        {
            generationInfo.maxScaledLimit = (int) element.getPropertyValue();
        }
        else if ("spawnX".equals(element.getID()))
        {
            BlockCoord coord = generationInfo.spawnShift;
            generationInfo.spawnShift = new BlockCoord((int) element.getPropertyValue(), coord.y, coord.z);
        }
        else if ("spawnY".equals(element.getID()))
        {
            BlockCoord coord = generationInfo.spawnShift;
            generationInfo.spawnShift = new BlockCoord(coord.x, (int) element.getPropertyValue(), coord.z);
        }
        else if ("spawnZ".equals(element.getID()))
        {
            BlockCoord coord = generationInfo.spawnShift;
            generationInfo.spawnShift = new BlockCoord(coord.x, coord.y, (int) element.getPropertyValue());
        }
    }
}
