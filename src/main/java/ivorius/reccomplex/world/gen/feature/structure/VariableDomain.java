/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure;

import gnu.trove.impl.unmodifiable.TUnmodifiableObjectByteMap;
import gnu.trove.map.TObjectByteMap;
import gnu.trove.map.hash.TObjectByteHashMap;
import ivorius.ivtoolkit.tools.NBTCompoundObject;
import net.minecraft.nbt.NBTTagCompound;

import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by lukas on 15.04.17.
 */
public class VariableDomain implements NBTCompoundObject
{
    protected final TObjectByteMap<String> variables = new TObjectByteHashMap<>();

    public VariableDomain()
    {
    }

    public VariableDomain(TObjectByteMap<String> variables)
    {
        this.variables.putAll(variables);
    }

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

    public VariableDomain set(String variable, boolean value)
    {
        variables.put(variable, (byte) (value ? 1 : 0));
        return this;
    }

    public Stream<VariableDomain> split(String variable)
    {
        return IntStream.of(0, 1)
                .mapToObj(i ->
                {
                    VariableDomain copy = copy();
                    copy.set(variable, i != 0);
                    return copy;
                });
    }

    public TObjectByteMap<String> all()
    {
        return new TUnmodifiableObjectByteMap<>(variables);
    }

    public VariableDomain copy()
    {
        return new VariableDomain(variables);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        variables.clear();
        for (String s : compound.getKeySet())
            set(s, compound.getBoolean(s));
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        variables.forEachEntry((a, b) ->
        {
            compound.setBoolean(a, b != 0);
            return true;
        });
    }
}
