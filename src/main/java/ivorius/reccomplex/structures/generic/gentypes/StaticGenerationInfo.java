/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.gentypes;

import com.google.gson.*;
import ivorius.reccomplex.gui.editstructure.gentypes.TableDataSourceStaticGenerationInfo;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.generic.matchers.DimensionMatcher;
import ivorius.reccomplex.structures.generic.GenerationYSelector;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.StatCollector;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;

/**
 * Created by lukas on 21.02.15.
 */
public class StaticGenerationInfo extends StructureGenerationInfo
{
    public String id = "";

    public GenerationYSelector ySelector;
    public DimensionMatcher dimensionMatcher;

    public boolean relativeToSpawn;
    public int positionX;
    public int positionZ;

    public StaticGenerationInfo()
    {
        this("StaticGen1", new GenerationYSelector(GenerationYSelector.SelectionMode.SURFACE, 0, 0), new DimensionMatcher("0"), true, 0, 0);
    }

    public StaticGenerationInfo(String id, GenerationYSelector ySelector, DimensionMatcher dimensionMatcher, boolean relativeToSpawn, int positionX, int positionZ)
    {
        this.id = id;
        this.ySelector = ySelector;
        this.dimensionMatcher = dimensionMatcher;
        this.relativeToSpawn = relativeToSpawn;
        this.positionX = positionX;
        this.positionZ = positionZ;
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
        if (relativeToSpawn)
            return StatCollector.translateToLocalFormatted("reccomplex.generationInfo.static.spawn", String.valueOf(positionX), String.valueOf(positionZ));
        else
            return StatCollector.translateToLocalFormatted("reccomplex.generationInfo.static.nospawn", String.valueOf(positionX), String.valueOf(positionZ));
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceStaticGenerationInfo(navigator, delegate, this);
    }

    public int getPositionX(ChunkCoordinates spawnPos)
    {
        return relativeToSpawn ? spawnPos.posX + positionX : positionX;
    }

    public int getPositionZ(ChunkCoordinates spawnPos)
    {
        return relativeToSpawn ? spawnPos.posZ + positionZ : positionZ;
    }

    public static class Serializer implements JsonSerializer<StaticGenerationInfo>, JsonDeserializer<StaticGenerationInfo>
    {
        @Override
        public StaticGenerationInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(json, "vanillaStructureSpawnInfo");

            String id = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "id", "");

            GenerationYSelector ySelector = context.deserialize(jsonObject.get("generationY"), GenerationYSelector.class);
            String dimension = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "dimensions", "");

            boolean relativeToSpawn = JsonUtils.getJsonObjectBooleanFieldValueOrDefault(jsonObject, "relativeToSpawn", false);
            int positionX = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "positionX", 0);
            int positionZ = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "positionZ", 0);

            return new StaticGenerationInfo(id, ySelector, new DimensionMatcher(dimension), relativeToSpawn, positionX, positionZ);
        }

        @Override
        public JsonElement serialize(StaticGenerationInfo src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", src.id);

            jsonObject.add("generationY", context.serialize(src.ySelector));
            jsonObject.addProperty("dimensions", src.dimensionMatcher.getExpression());

            jsonObject.addProperty("relativeToSpawn", src.relativeToSpawn);
            jsonObject.addProperty("positionX", src.positionX);
            jsonObject.addProperty("positionZ", src.positionZ);

            return jsonObject;
        }
    }
}
