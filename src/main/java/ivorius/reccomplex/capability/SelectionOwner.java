/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.capability;

import ivorius.ivtoolkit.blocks.BlockArea;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;

/**
 * Created by lukas on 06.10.16.
 */
public interface SelectionOwner
{
    @Nullable
    static SelectionOwner getOwner(Object object, @Nullable EnumFacing facing)
    {
        if (object instanceof SelectionOwner)
            return (SelectionOwner) object;

        if (object instanceof ICapabilityProvider)
            return CapabilitySelection.get((ICapabilityProvider) object, facing);

        return null;
    }

    default boolean hasValidSelection()
    {
        return getSelection() != null;
    }

    @Nullable
    BlockPos getSelectedPoint1();

    void setSelectedPoint1(@Nullable BlockPos pos);

    @Nullable
    BlockPos getSelectedPoint2();

    void setSelectedPoint2(@Nullable BlockPos pos);

    @Nullable
    default BlockArea getSelection()
    {
        BlockPos l = getSelectedPoint1();
        BlockPos r = getSelectedPoint2();
        return l != null && r != null ? new BlockArea(l, r) : null;
    }

    default void setSelection(@Nullable BlockArea area)
    {
        setSelectedPoint1(area != null ? area.getPoint1() : null);
        setSelectedPoint2(area != null ? area.getPoint2() : null);
    }
}
