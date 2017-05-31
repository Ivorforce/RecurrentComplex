/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Collectors;

import static net.minecraft.command.CommandBase.getListOfStringsMatchingLastWord;

/**
 * Created by lukas on 31.05.17.
 */
public class MCExpect<T extends MCExpect<T>> extends Expect<T>
{
    MCExpect()
    {

    }

    public static <T extends MCExpect<T>> T startMC()
    {
        //noinspection unchecked
        return (T) new MCExpect();
    }

    public T pos()
    {
        int index = index();
        return next((ser, sen, args, pos) -> CommandBase.getTabCompletionCoordinate(args, index, pos))
                .next((ser, sen, args, pos) -> CommandBase.getTabCompletionCoordinate(args, index, pos))
                .next((ser, sen, args, pos) -> CommandBase.getTabCompletionCoordinate(args, index, pos));
    }

    public T biome()
    {
        return next(Biome.REGISTRY.getKeys());
    }

    public T dimension()
    {
        return next(args -> getListOfStringsMatchingLastWord(args, Arrays.stream(DimensionManager.getIDs()).map(String::valueOf).collect(Collectors.toList())));
    }

    public T block()
    {
        return next(Block.REGISTRY.getKeys());
    }
}
