/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature;

import ivorius.ivtoolkit.tools.NBTTagLists;
import ivorius.reccomplex.RecurrentComplex;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by lukas on 01.03.15.
 */
public class WorldRandomData extends WorldSavedData
{
    private static final String IDENTIFIER = RecurrentComplex.MOD_ID + "-random";

    private final Set<String> showedWorldStatus = new HashSet<>();

    public WorldRandomData(String id)
    {
        super(id);
    }

    public WorldRandomData()
    {
        this(IDENTIFIER);
    }

    public static WorldRandomData get(World world)
    {
        WorldRandomData data = (WorldRandomData) world.loadData(WorldRandomData.class, IDENTIFIER);
        if (data == null)
        {
            data = new WorldRandomData();
            world.setData(data.mapName, data);
        }
        return data;
    }

    public boolean postWorldStatus(String player)
    {
        boolean add = showedWorldStatus.add(player);
        if (add) setDirty(true);
        return add;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        showedWorldStatus.clear();
        for (NBTBase nbt : NBTTagLists.nbtBases(compound.getTagList("showedWorldStatus", Constants.NBT.TAG_STRING)))
            showedWorldStatus.add(((NBTTagString) nbt).getString());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        NBTTagLists.writeTo(compound, "showedWorldStatus", showedWorldStatus.stream().map(NBTTagString::new).collect(Collectors.toList()));

        return compound;
    }
}
