/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.transformers;

import com.google.gson.JsonObject;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.world.gen.feature.structure.*;
import ivorius.reccomplex.nbt.NBTStorable;
import ivorius.reccomplex.world.gen.feature.structure.context.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.Random;
import java.util.Stack;

/**
 * Created by lukas on 25.05.14.
 */
public abstract class Transformer<S extends NBTStorable>
{
    // Legacy for missing IDs
    public static Stack<Random> idRandomizers = new Stack<>();

    static {
        idRandomizers.push(new Random(0xDEADBEEF));
    }

    @Nonnull
    protected String id;

    public Transformer(@Nonnull String id)
    {
        this.id = id;
    }

    public static String randomID(Class<? extends Transformer> type)
    {
        Random random = new Random();
        return String.format("%s_%s", StructureRegistry.TRANSFORMERS.iDForType(type), Integer.toHexString(random.nextInt()));
    }

    public static String randomID(String type)
    {
        Random random = new Random();
        return String.format("%s_%s", type, Integer.toHexString(random.nextInt()));
    }

    public static String readID(JsonObject object)
    {
        String id = JsonUtils.getString(object, "id", null);
        if (id == null) id = Integer.toHexString(idRandomizers.peek().nextInt()); // Legacy support for missing IDs
        return id;
    }

    @Nonnull
    public String id()
    {
        return id;
    }

    public void setID(@Nonnull String id)
    {
        this.id = id;
    }

    public abstract String getDisplayString();

    public abstract TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate);

    public abstract S prepareInstanceData(StructurePrepareContext context, IvWorldData worldData);

    public void configureInstanceData(S s, StructurePrepareContext context, IvWorldData worldData, RunTransformer transformer)
    {

    }

    public abstract S loadInstanceData(StructureLoadContext context, NBTBase nbt);

    public boolean mayGenerate(S instanceData, StructurePrepareContext context, IvWorldData worldData, RunTransformer transformer)
    {
        return true;
    }

    public boolean skipGeneration(S instanceData, StructureLiveContext context, BlockPos pos, IBlockState state, IvWorldData worldData, BlockPos sourcePos)
    {
        return false;
    }

    public void transform(S instanceData, Phase phase, StructureSpawnContext context, IvWorldData worldData, RunTransformer transformer)
    {

    }

    public enum Phase
    {
        BEFORE,
        AFTER
    }
}
