/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.inventorygen;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.table.screen.GuiScreenEditTable;
import ivorius.reccomplex.network.PacketSaveInvGenComponent;
import ivorius.reccomplex.utils.SaveDirectoryData;
import ivorius.reccomplex.world.storage.loot.GenericItemCollection;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by lukas on 27.08.16.
 */

@SideOnly(Side.CLIENT)
public class GuiEditInventoryGen extends GuiScreenEditTable<TableDataSourceItemCollectionComponent>
{
    public GuiEditInventoryGen(EntityPlayer player, GenericItemCollection.Component component, String key, SaveDirectoryData saveDirectoryData)
    {
        setDataSource(new TableDataSourceItemCollectionComponent(key, component, saveDirectoryData, player, this, this), ds ->
                RecurrentComplex.network.sendToServer(new PacketSaveInvGenComponent(ds.key, ds.component, ds.getSaveDirectoryData().getResult())));
    }
}
