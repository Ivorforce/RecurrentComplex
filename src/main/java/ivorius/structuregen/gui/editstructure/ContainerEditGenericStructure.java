package ivorius.structuregen.gui.editstructure;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

/**
 * Created by lukas on 26.05.14.
 */
public class ContainerEditGenericStructure extends Container
{
    @Override
    public boolean canInteractWith(EntityPlayer var1)
    {
        return true;
    }
}
