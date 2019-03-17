/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.tweak;

import gnu.trove.map.TObjectFloatMap;
import ivorius.reccomplex.gui.table.screen.GuiScreenEditTable;
import ivorius.reccomplex.network.PacketSpawnTweaksHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiTweakStructures extends GuiScreenEditTable<TableDataSourceTweakStructures>
{
    public GuiTweakStructures(TObjectFloatMap<String> tweaks)
    {
        setDataSource(new TableDataSourceTweakStructures(this, tweaks), tds -> {
            PacketSpawnTweaksHandler.sendToServer(tds.tweaks);
        });
    }
}
