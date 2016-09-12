/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.gentypes;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.editstructure.gentypes.TableDataSourceStaticGenerationInfo;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.generic.GenericYSelector;
import ivorius.reccomplex.structures.generic.matchers.DimensionMatcher;
import ivorius.reccomplex.utils.BlockSurfacePos;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;

/**
 * Created by lukas on 21.02.15.
 */
public class StaticGenerationInfo extends StructureGenerationInfo
{
    private static Gson gson = createGson();

    public GenericYSelector ySelector;
    public DimensionMatcher dimensionMatcher;

    public boolean relativeToSpawn;
    public BlockSurfacePos position;

    @Nullable
    public Pattern pattern;

    public StaticGenerationInfo()
    {
        this(null, new GenericYSelector(GenericYSelector.SelectionMode.SURFACE, 0, 0), new DimensionMatcher("0"), true, BlockSurfacePos.ORIGIN, null);
    }

    public StaticGenerationInfo(@Nullable String id, GenericYSelector ySelector, DimensionMatcher dimensionMatcher, boolean relativeToSpawn, BlockSurfacePos position, Pattern pattern)
    {
        super(id != null ? id : randomID(StaticGenerationInfo.class));
        this.ySelector = ySelector;
        this.dimensionMatcher = dimensionMatcher;
        this.relativeToSpawn = relativeToSpawn;
        this.position = position;
        this.pattern = pattern;
    }

    public static Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(StaticGenerationInfo.class, new StaticGenerationInfo.Serializer());
        builder.registerTypeAdapter(GenericYSelector.class, new GenericYSelector.Serializer());

        return builder.create();
    }

    public static Gson getGson()
    {
        return gson;
    }

    public BlockSurfacePos getPosition()
    {
        return position;
    }

    public void setPosition(BlockSurfacePos position)
    {
        this.position = position;
    }

    @Nonnull
    @Override
    public String id()
    {
        return id;
    }

    @Override
    public void setID(@Nonnull String id)
    {
        this.id = id;
    }

    @Override
    public String displayString()
    {
        if (hasPattern())
            return IvTranslations.format("reccomplex.generationInfo.static.summary.pattern", String.valueOf(pattern.repeatX), String.valueOf(pattern.repeatZ));
        else if (relativeToSpawn)
            return IvTranslations.format("reccomplex.generationInfo.static.summary.spawn", String.valueOf(position.x), String.valueOf(position.z));
        else
            return IvTranslations.format("reccomplex.generationInfo.static.summary.nospawn", String.valueOf(position.x), String.valueOf(position.z));
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceStaticGenerationInfo(navigator, delegate, this);
    }

    public BlockSurfacePos getPos(BlockPos spawnPos)
    {
        return new BlockSurfacePos(relativeToSpawn ? spawnPos.getX() + position.x : position.x, relativeToSpawn ? spawnPos.getZ() + position.z : position.z);
    }

    public boolean hasPattern()
    {
        return pattern != null;
    }

    public static class Pattern
    {
        @SerializedName("repeatX")
        public int repeatX = 16;
        @SerializedName("repeatZ")
        public int repeatZ = 16;

        @SerializedName("randomShiftX")
        public int randomShiftX = 0;
        @SerializedName("randomShiftZ")
        public int randomShiftZ = 0;
    }

    public static class Serializer implements JsonSerializer<StaticGenerationInfo>, JsonDeserializer<StaticGenerationInfo>
    {
        @Override
        public StaticGenerationInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(json, "vanillaStructureSpawnInfo");

            String id = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "id", null);

            GenericYSelector ySelector = gson.fromJson(jsonObject.get("generationY"), GenericYSelector.class);
            String dimension = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "dimensions", "");

            boolean relativeToSpawn = JsonUtils.getJsonObjectBooleanFieldValueOrDefault(jsonObject, "relativeToSpawn", false);
            int positionX = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "positionX", 0);
            int positionZ = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "positionZ", 0);

            Pattern pattern = jsonObject.has("pattern") ? gson.fromJson(jsonObject.get("pattern"), Pattern.class) : null;

            return new StaticGenerationInfo(id, ySelector, new DimensionMatcher(dimension), relativeToSpawn, new BlockSurfacePos(positionX, positionZ), pattern);
        }

        @Override
        public JsonElement serialize(StaticGenerationInfo src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", src.id);

            jsonObject.add("generationY", gson.toJsonTree(src.ySelector));
            jsonObject.addProperty("dimensions", src.dimensionMatcher.getExpression());

            jsonObject.addProperty("relativeToSpawn", src.relativeToSpawn);
            jsonObject.addProperty("positionX", src.position.x);
            jsonObject.addProperty("positionZ", src.position.z);

            if (src.pattern != null)
                jsonObject.add("pattern", gson.toJsonTree(src.pattern));

            return jsonObject;
        }
    }
}
