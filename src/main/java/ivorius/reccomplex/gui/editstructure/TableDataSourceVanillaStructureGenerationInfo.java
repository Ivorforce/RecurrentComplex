/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;

/**
 * Created by lukas on 07.10.14.
 */
public class TableDataSourceVanillaStructureGenerationInfo extends TableDataSourceSegmented implements TableElementPropertyListener
{
    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    private GenericStructureInfo structureInfo;

    public TableDataSourceVanillaStructureGenerationInfo(TableNavigator navigator, TableDelegate tableDelegate, GenericStructureInfo structureInfo)
    {
        this.navigator = navigator;
        this.tableDelegate = tableDelegate;
        this.structureInfo = structureInfo;
    }

    @Override
    public int numberOfSegments()
    {
        return 4;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
            case 0:
                return 1;
            case 1:
                return 1;
            case 2:
                return 4;
            case 3:
                return 3;
        }
        return 0;
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        switch (segment)
        {
            case 0:
            {
                TableElementList element = new TableElementList("type", "Type", "village", new TableElementList.Option("village", "Village"));
                element.addPropertyListener(this);
                return element;
            }
            case 1:
            {
                TableElementInteger element = new TableElementInteger("spawnWeight", "Spawn Weight", structureInfo.vanillaStructureSpawnInfo.spawnWeight, 1, 200);
                element.addPropertyListener(this);
                return element;
            }
            case 2:
                switch (index)
                {
                    case 0:
                    {
                        TableElementInteger element = new TableElementInteger("minBaseLimit", "Min. Base Limit", structureInfo.vanillaStructureSpawnInfo.minBaseLimit, 0, 100);
                        element.addPropertyListener(this);
                        return element;
                    }
                    case 1:
                    {
                        TableElementInteger element = new TableElementInteger("maxBaseLimit", "Max. Base Limit", structureInfo.vanillaStructureSpawnInfo.maxBaseLimit, 0, 100);
                        element.addPropertyListener(this);
                        return element;
                    }
                    case 2:
                    {
                        TableElementInteger element = new TableElementInteger("minScaleLimit", "Min. Scaled Limit", structureInfo.vanillaStructureSpawnInfo.minScaledLimit, 0, 100);
                        element.addPropertyListener(this);
                        return element;
                    }
                    case 3:
                    {
                        TableElementInteger element = new TableElementInteger("maxScaleLimit", "Max. Scaled Limit", structureInfo.vanillaStructureSpawnInfo.maxScaledLimit, 0, 100);
                        element.addPropertyListener(this);
                        return element;
                    }
                }
                break;
            case 3:
                switch (index)
                {
                    case 0:
                    {
                        TableElementInteger element = new TableElementInteger("spawnX", "Spawn Shift X", structureInfo.vanillaStructureSpawnInfo.spawnShift.x, -50, 50);
                        element.addPropertyListener(this);
                        return element;
                    }
                    case 1:
                    {
                        TableElementInteger element = new TableElementInteger("spawnY", "Spawn Shift Y", structureInfo.vanillaStructureSpawnInfo.spawnShift.y, -50, 50);
                        element.addPropertyListener(this);
                        return element;
                    }
                    case 2:
                    {
                        TableElementInteger element = new TableElementInteger("spawnZ", "Spawn Shift Z", structureInfo.vanillaStructureSpawnInfo.spawnShift.z, -50, 50);
                        element.addPropertyListener(this);
                        return element;
                    }
                }
                break;
        }

        return null;
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        if ("spawnWeight".equals(element.getID()))
        {
            structureInfo.vanillaStructureSpawnInfo.spawnWeight = (int) element.getPropertyValue();
        }
        else if ("minBaseLimit".equals(element.getID()))
        {
            structureInfo.vanillaStructureSpawnInfo.minBaseLimit = (int) element.getPropertyValue();
        }
        else if ("maxBaseLimit".equals(element.getID()))
        {
            structureInfo.vanillaStructureSpawnInfo.maxBaseLimit = (int) element.getPropertyValue();
        }
        else if ("minScaledLimit".equals(element.getID()))
        {
            structureInfo.vanillaStructureSpawnInfo.minScaledLimit = (int) element.getPropertyValue();
        }
        else if ("maxScaledLimit".equals(element.getID()))
        {
            structureInfo.vanillaStructureSpawnInfo.maxScaledLimit = (int) element.getPropertyValue();
        }
        else if ("spawnX".equals(element.getID()))
        {
            BlockCoord coord = structureInfo.vanillaStructureSpawnInfo.spawnShift;
            structureInfo.vanillaStructureSpawnInfo.spawnShift = new BlockCoord((int) element.getPropertyValue(), coord.y, coord.z);
        }
        else if ("spawnY".equals(element.getID()))
        {
            BlockCoord coord = structureInfo.vanillaStructureSpawnInfo.spawnShift;
            structureInfo.vanillaStructureSpawnInfo.spawnShift = new BlockCoord(coord.x, (int) element.getPropertyValue(), coord.z);
        }
        else if ("spawnZ".equals(element.getID()))
        {
            BlockCoord coord = structureInfo.vanillaStructureSpawnInfo.spawnShift;
            structureInfo.vanillaStructureSpawnInfo.spawnShift = new BlockCoord(coord.x, coord.y, (int) element.getPropertyValue());
        }
    }
}
