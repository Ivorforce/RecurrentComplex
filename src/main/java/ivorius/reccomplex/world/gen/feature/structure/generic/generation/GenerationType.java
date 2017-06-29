/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.generation;

import com.google.gson.JsonObject;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.Placer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;
import java.util.Stack;

/**
 * Created by lukas on 19.02.15.
 */
public abstract class GenerationType
{
    // Legacy for missing IDs
    public static Stack<Random> idRandomizers = new Stack<>();

    static {
        idRandomizers.push(new Random(0xDEADBEEF));
    }

    @Nonnull
    protected String id;

    public GenerationType(@Nonnull String id)
    {
        this.id = id;
    }

    public static String randomID(Class<? extends GenerationType> type)
    {
        Random random = new Random();
        return String.format("%s_%s", StructureRegistry.GENERATION_TYPES.iDForType(type), Integer.toHexString(random.nextInt()));
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

    public abstract String displayString();

    @Nullable
    public abstract Placer placer();

    public abstract TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate);
}
