/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.ivtoolkit.maze;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.WeightedRandom;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 20.06.14.
 */
public class MazeComponent extends WeightedRandom.Item
{
    private String identifier;

    private List<MazeRoom> rooms = new ArrayList<>();
    private List<MazePath> exitPaths = new ArrayList<>();

    public MazeComponent(int par1, String identifier, List<MazeRoom> rooms, List<MazePath> exitPaths)
    {
        super(par1);
        this.identifier = identifier;
        this.rooms.addAll(rooms);
        this.exitPaths.addAll(exitPaths);
    }

    public MazeComponent(NBTTagCompound compound)
    {
        super(compound.getInteger("weight"));

        identifier = compound.getString("identifier");

        NBTTagList roomsList = compound.getTagList("rooms", Constants.NBT.TAG_COMPOUND);
        rooms.clear();
        for (int i = 0; i < roomsList.tagCount(); i++)
        {
            rooms.add(new MazeRoom(roomsList.getCompoundTagAt(i)));
        }

        NBTTagList exitsList = compound.getTagList("exits", Constants.NBT.TAG_COMPOUND);
        exitPaths.clear();
        for (int i = 0; i < exitsList.tagCount(); i++)
        {
            exitPaths.add(new MazePath(exitsList.getCompoundTagAt(i)));
        }
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public List<MazeRoom> getRooms()
    {
        return Collections.unmodifiableList(rooms);
    }

    public void setRooms(List<MazeRoom> rooms)
    {
        this.rooms.clear();
        this.rooms.addAll(rooms);
    }

    public List<MazePath> getExitPaths()
    {
        return Collections.unmodifiableList(exitPaths);
    }

    public void setExitPaths(List<MazePath> exitPaths)
    {
        this.exitPaths.clear();
        this.exitPaths.addAll(exitPaths);
    }

    public int[] getSize()
    {
        int[] size = new int[]{1, 1, 1};
        for (MazeRoom room : rooms)
        {
            if (room.coordinates[0] >= size[0])
            {
                size[0] = room.coordinates[0] + 1;
            }
            else if (room.coordinates[1] >= size[1])
            {
                size[1] = room.coordinates[1] + 1;
            }
            else if (room.coordinates[2] >= size[2])
            {
                size[2] = room.coordinates[2] + 1;
            }
        }
        return size;
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        compound.setString("identifier", identifier);
        compound.setInteger("weight", itemWeight);

        NBTTagList roomsList = new NBTTagList();
        for (MazeRoom room : rooms)
        {
            roomsList.appendTag(room.writeToNBT());
        }
        compound.setTag("rooms", roomsList);

        NBTTagList exitsList = new NBTTagList();
        for (MazePath exit : exitPaths)
        {
            exitsList.appendTag(exit.writeToNBT());
        }
        compound.setTag("exits", exitsList);
    }
}
