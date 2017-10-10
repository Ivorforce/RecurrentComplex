/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.client.rendering.MazeVisualizationContext;
import ivorius.reccomplex.gui.table.screen.GuiScreenEditTable;
import ivorius.reccomplex.network.PacketSaveStructureHandler;
import ivorius.reccomplex.utils.SaveDirectoryData;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by lukas on 26.05.14.
 */

@SideOnly(Side.CLIENT)
public class GuiEditGenericStructure extends GuiScreenEditTable<TableDataSourceGenericStructure>
{
    public GuiEditGenericStructure(String key, GenericStructure structureInfo, BlockPos lowerCoord, SaveDirectoryData data)
    {
        setDataSource(new TableDataSourceGenericStructure(structureInfo, key, data, this, this, new MazeVisualizationContext(lowerCoord, null)), ds ->
                PacketSaveStructureHandler.saveStructure(ds.getStructureInfo(), ds.getStructureKey(), ds.getSaveDirectoryData().getResult()));
    }
}
