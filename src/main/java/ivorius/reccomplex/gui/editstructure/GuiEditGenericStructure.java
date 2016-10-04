/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.GuiScreenEditTable;
import ivorius.reccomplex.gui.table.Bounds;
import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.network.PacketSaveStructureHandler;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructureInfo;
import ivorius.reccomplex.utils.SaveDirectoryData;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

/**
 * Created by lukas on 26.05.14.
 */
public class GuiEditGenericStructure extends GuiScreenEditTable<TableDataSourceGenericStructure>
{
    public GuiEditGenericStructure(String key, GenericStructureInfo structureInfo, SaveDirectoryData data)
    {
        setDataSource(new TableDataSourceGenericStructure(structureInfo, key, data, this, this), ds -> {
            PacketSaveStructureHandler.saveStructure(ds.getStructureInfo(), ds.getStructureKey(), ds.getSaveDirectoryData().getResult());
        });
    }
}
