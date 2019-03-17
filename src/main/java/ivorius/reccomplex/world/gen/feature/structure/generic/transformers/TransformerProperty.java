/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.transformers;

import com.google.gson.*;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.editstructure.transformers.TableDataSourceBTProperty;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.utils.algebra.ExpressionCache;
import ivorius.reccomplex.world.gen.feature.structure.Environment;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureLoadContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructurePrepareContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureSpawnContext;
import ivorius.reccomplex.utils.expression.BlockExpression;
import ivorius.reccomplex.nbt.NBTNone;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by lukas on 25.05.14.
 */
public class TransformerProperty extends TransformerSingleBlock<NBTNone>
{
    public BlockExpression sourceMatcher;
    public String propertyName = "";
    public String propertyValue = "";

    public TransformerProperty()
    {
        this(null, "", "", "");
    }

    public TransformerProperty(@Nullable String id, String sourceExpression, String propertyName, String propertyValue)
    {
        super(id != null ? id : randomID(TransformerProperty.class));
        this.sourceMatcher = ExpressionCache.of(new BlockExpression(RecurrentComplex.specialRegistry), sourceExpression);
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
    }

    @Nonnull
    public static <T extends Comparable<T>> Optional<IBlockState> withProperty(IBlockState state, String propertyName, String propertyValue)
    {
        @SuppressWarnings("unchecked") Optional<IProperty<T>> mProperty = (Optional) state.getProperties().keySet().stream().filter(p -> p.getName().equals(propertyName)).findFirst();

        if (mProperty.isPresent())
        {
            IProperty<T> property = mProperty.get();
            return property.getAllowedValues().stream()
                    .filter(v1 -> property.getName((T) v1).equals(propertyValue)).findAny()
                    .map(v -> state.withProperty(property, v));
        }
        return Optional.empty();
    }

    public static Stream<String> propertyValueStream(String propertyName)
    {
        return Block.REGISTRY.getKeys().stream().map(Block.REGISTRY::getObject)
                .map(b -> b.getDefaultState().getPropertyKeys().stream().filter(p -> p.getName().equals(propertyName)).findFirst().orElse(null))
                .filter(Objects::nonNull).flatMap(p -> p.getAllowedValues().stream().map(p::getName));
    }

    public static Stream<String> propertyNameStream()
    {
        return Block.REGISTRY.getKeys().stream().map(Block.REGISTRY::getObject)
                .flatMap(b -> b.getDefaultState().getPropertyKeys().stream().map(IProperty::getName));
    }

    @Override
    public boolean matches(Environment environment, NBTNone instanceData, BlockPos sourcePos, IBlockState state)
    {
        return sourceMatcher.test(state) && withProperty(state, propertyName, propertyValue).isPresent();
    }

    @Override
    public void transformBlock(NBTNone instanceData, Phase phase, StructureSpawnContext context, RunTransformer transformer, int[] areaSize, BlockPos coord, IBlockState sourceState)
    {
        withProperty(sourceState, propertyName, propertyValue)
                .ifPresent(state -> context.setBlock(coord, state, 2));
    }

    @Override
    public NBTNone prepareInstanceData(StructurePrepareContext context, IvWorldData worldData)
    {
        return new NBTNone();
    }

    @Override
    public NBTNone loadInstanceData(StructureLoadContext context, NBTBase nbt)
    {
        return new NBTNone();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public String getDisplayString()
    {
        return TextFormatting.GREEN + propertyName + TextFormatting.RESET + " -> " + TextFormatting.GREEN + propertyValue + TextFormatting.RESET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceBTProperty(this, navigator, delegate);
    }

    @Override
    public boolean generatesInPhase(NBTNone instanceData, Phase phase)
    {
        return phase == Phase.BEFORE;
    }

    public static class Serializer implements JsonDeserializer<TransformerProperty>, JsonSerializer<TransformerProperty>
    {
        private MCRegistry registry;

        public Serializer(MCRegistry registry)
        {
            this.registry = registry;
        }

        @Override
        public TransformerProperty deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonObject = JsonUtils.asJsonObject(jsonElement, "transformerPropertyReplace");

            String id = readID(jsonObject);

            String expression = JsonUtils.getString(jsonObject, "sourceExpression", "");
            String propertyName = JsonUtils.getString(jsonObject, "propertyName", "");
            String propertyValue = JsonUtils.getString(jsonObject, "propertyValue", "");

            return new TransformerProperty(id, expression, propertyName, propertyValue);
        }

        @Override
        public JsonElement serialize(TransformerProperty transformer, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", transformer.id());

            jsonObject.addProperty("sourceExpression", transformer.sourceMatcher.getExpression());
            jsonObject.addProperty("propertyName", transformer.propertyName);
            jsonObject.addProperty("propertyValue", transformer.propertyValue);

            return jsonObject;
        }
    }
}
