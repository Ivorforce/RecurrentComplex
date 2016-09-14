/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.structures.generic.gentypes;

import com.google.gson.*;
import ivorius.ivtoolkit.blocks.Directions;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.editstructure.gentypes.TableDataSourceSaplingGenerationInfo;
import ivorius.reccomplex.gui.editstructure.gentypes.TableDataSourceVanillaStructureGenerationInfo;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.Environment;
import ivorius.reccomplex.structures.YSelector;
import ivorius.reccomplex.structures.generic.GenericYSelector;
import ivorius.reccomplex.structures.generic.matchers.BiomeMatcher;
import ivorius.reccomplex.structures.generic.matchers.BlockMatcher;
import ivorius.reccomplex.structures.generic.matchers.EnvironmentMatcher;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;

/**
 * Created by lukas on 19.01.15.
 */
public class SaplingGenerationInfo extends StructureGenerationInfo
{
    public Double generationWeight;

    public BlockPos spawnShift;

    public EnvironmentMatcher environmentMatcher;
    public BlockMatcher blockMatcher;

    public SaplingGenerationInfo()
    {
        this(null, null, BlockPos.ORIGIN, "", "");
    }

    public SaplingGenerationInfo(@Nullable String id, Double generationWeight, BlockPos spawnShift, String environmentExpression, String blockExpression)
    {
        super(id != null ? id : randomID(SaplingGenerationInfo.class));
        this.generationWeight = generationWeight;
        this.spawnShift = spawnShift;
        environmentMatcher = new EnvironmentMatcher(environmentExpression);
        blockMatcher = new BlockMatcher(RecurrentComplex.specialRegistry, blockExpression);
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

    public void setSpawnShift(BlockPos spawnShift)
    {
        this.spawnShift = spawnShift;
    }

    @Override
    public String displayString()
    {
        return IvTranslations.get("reccomplex.generationInfo.sapling.title");
    }

    @Nullable
    @Override
    public YSelector ySelector()
    {
        return new GenericYSelector(GenericYSelector.SelectionMode.SURFACE, spawnShift.getY(), spawnShift.getY());
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceSaplingGenerationInfo(navigator, delegate, this);
    }

    public boolean generatesIn(Environment environment)
    {
        return environmentMatcher.test(environment);
    }

    public boolean generatesFor(IBlockState state)
    {
        return blockMatcher.test(state);
    }

    public boolean generatesFor(Environment environment, IBlockState state)
    {
        return environmentMatcher.test(environment) && generatesFor(state);
    }

    public double getActiveWeight()
    {
        return generationWeight != null ? generationWeight : 1.0;
    }

    public int getVanillaWeight()
    {
        return MathHelper.floor_double(getActiveWeight() * RCConfig.baseVillageSpawnWeight + 0.5);
    }

    public static class Serializer implements JsonSerializer<SaplingGenerationInfo>, JsonDeserializer<SaplingGenerationInfo>
    {
        @Override
        public SaplingGenerationInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.asJsonObject(json, "vanillaStructureSpawnInfo");

            String id = JsonUtils.getString(jsonObject, "id", null);

            Double spawnWeight = jsonObject.has("generationWeight") ? JsonUtils.getDouble(jsonObject, "generationWeight") : null;

            int spawnX = JsonUtils.getInt(jsonObject, "spawnShiftX", 0);
            int spawnY = JsonUtils.getInt(jsonObject, "spawnShiftY", 0);
            int spawnZ = JsonUtils.getInt(jsonObject, "spawnShiftZ", 0);

            String environmentExpression = JsonUtils.getString(jsonObject, "environmentExpression", "");
            String blockExpression = JsonUtils.getString(jsonObject, "blockExpression", "");

            return new SaplingGenerationInfo(id, spawnWeight, new BlockPos(spawnX, spawnY, spawnZ), environmentExpression, blockExpression);
        }

        @Override
        public JsonElement serialize(SaplingGenerationInfo src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", src.id);

            if (src.generationWeight != null)
                jsonObject.addProperty("generationWeight", src.generationWeight);

            jsonObject.addProperty("spawnShiftX", src.spawnShift.getX());
            jsonObject.addProperty("spawnShiftY", src.spawnShift.getY());
            jsonObject.addProperty("spawnShiftZ", src.spawnShift.getZ());

            jsonObject.addProperty("environmentExpression", src.environmentMatcher.getExpression());
            jsonObject.addProperty("blockExpression", src.blockMatcher.getExpression());

            return jsonObject;
        }
    }
}
