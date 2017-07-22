/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure;

import ivorius.reccomplex.nbt.NBTStorable;
import ivorius.reccomplex.world.gen.feature.StructureGenerator;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by lukas on 08.03.17.
 */
public class ReadableInstanceData<T extends NBTStorable>
{
    private NBTBase instanceDataNBT;
    private T instanceData;

    public boolean exists()
    {
        return instanceData != null || instanceDataNBT != null;
    }

    public void setInstanceData(T instanceData)
    {
        this.instanceData = instanceData;
    }

    public void writeToNBT(String key, NBTTagCompound compound)
    {
        NBTBase instanceDataNBT = instanceData != null ? instanceData.writeToNBT()
                : this.instanceDataNBT != null ? this.instanceDataNBT.copy() : null;
        if (instanceDataNBT != null)
            compound.setTag(key, instanceDataNBT);
    }

    public void readFromNBT(String key, NBTTagCompound compound)
    {
        instanceDataNBT = compound.hasKey(key) ? compound.getTag(key).copy() : null;
    }

    public boolean load(StructureGenerator<T> generator)
    {
        if (instanceDataNBT != null)
        {
            generator.instanceData(instanceDataNBT);
            instanceData = generator.instanceData().get();
            instanceDataNBT = null;
        }

        return instanceData != null;
    }

    public StructureGenerator<T> register(StructureGenerator<T> generator)
    {
        if (instanceData != null)
            return generator.instanceData(instanceData);
        else if (instanceDataNBT != null)
            return generator.instanceData(instanceDataNBT);
        else
            return generator;
    }

    public void set(ReadableInstanceData<T> instanceData)
    {
        this.instanceData = instanceData.instanceData;
        this.instanceDataNBT = instanceData.instanceDataNBT;
    }
}
