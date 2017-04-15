/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure;

import gnu.trove.map.TObjectByteMap;
import gnu.trove.map.hash.TObjectByteHashMap;
import ivorius.ivtoolkit.tools.NBTCompoundObject;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by lukas on 15.04.17.
 */
public class VariableDomain implements NBTCompoundObject
{
    private final TObjectByteMap<String> variables = new TObjectByteHashMap<>();

    public void fill(VariableDomain domain)
    {
        variables.forEachEntry((a, b) ->
        {
            domain.variables.putIfAbsent(a, b);
            return true;
        });
    }

    public boolean get(String variable)
    {
        return variables.get(variable) != 0;
    }

    public boolean isSet(String variable)
    {
        return variables.containsKey(variable);
    }

    public void set(String variable, boolean value)
    {
        variables.put(variable, (byte) (value ? 1 : 0));
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        variables.forEachEntry((a, b) ->
        {
            compound.setBoolean(a, b != 0);
            return true;
        });
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        variables.clear();
        for (String s : compound.getKeySet())
            set(s, compound.getBoolean(s));
    }
}
