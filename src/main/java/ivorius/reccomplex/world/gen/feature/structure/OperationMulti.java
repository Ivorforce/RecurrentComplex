/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure;

import ivorius.ivtoolkit.tools.NBTTagLists;
import ivorius.reccomplex.operation.Operation;
import ivorius.reccomplex.operation.OperationRegistry;
import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lukas on 25.10.16.
 */
public class OperationMulti implements Operation
{
    public final List<Operation> operations = new ArrayList<>();

    public OperationMulti()
    {
    }

    public OperationMulti(List<Operation> operations)
    {
        this.operations.addAll(operations);
    }

    public OperationMulti(Operation... operations)
    {
        Collections.addAll(this.operations, operations);
    }

    @Override
    public void perform(WorldServer world)
    {
        operations.forEach(o -> o.perform(world));
    }

    @Override
    public void update(World world, int ticks)
    {
        operations.forEach(o -> o.update(world, ticks));
    }

    @Override
    public boolean checkDead(ICommandSender target)
    {
        return operations.stream().anyMatch(o -> o.checkDead(target));
    }

    @Override
    public void renderPreview(PreviewType previewType, World world, int ticks, float partialTicks)
    {
        for (int i = operations.size() - 1; i >= 0; i--) // Hax mostly for /#move
            operations.get(i).renderPreview(previewType, world, ticks, partialTicks);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        operations.clear();
        NBTTagLists.compoundsFrom(compound, "operations").stream().map(OperationRegistry::readOperation).forEach(operations::add);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        NBTTagLists.writeTo(compound, "operations", operations.stream().map(OperationRegistry::writeOperation).collect(Collectors.toList()));
    }
}
