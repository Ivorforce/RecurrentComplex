/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.structures.generic;

import com.google.common.collect.Multimap;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.ivtoolkit.tools.GuavaCollectors;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.ivtoolkit.tools.NBTCompoundObject;
import ivorius.ivtoolkit.tools.NBTCompoundObjects;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.generic.matchers.BlockMatcher;
import ivorius.reccomplex.utils.Transforms;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by lukas on 15.09.16.
 */
public class BlockPattern implements NBTCompoundObject
{
    public static final Gson gson = createGson();
    public final Selection pattern = new Selection(3);
    public final List<Ingredient> ingredients = new ArrayList<>();

    public BlockPattern(List<Selection.Area> pattern, List<Ingredient> ingredients)
    {
        this.pattern.addAll(pattern);
        this.ingredients.addAll(ingredients);
    }

    public BlockPattern()
    {
        pattern.add(new Selection.Area(true, new int[]{0, 0, 0}, new int[]{0, 0, 0}, "Sapling"));
        ingredients.add(new Ingredient("Sapling", new BlockMatcher(RecurrentComplex.specialRegistry, "id=minecraft:sapling"), true));
    }

    private static Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(BlockPattern.class, new Serializer());
        builder.registerTypeAdapter(Ingredient.class, new IngredientSerializer(RecurrentComplex.specialRegistry));

        return builder.create();
    }

    @Nonnull
    public static BlockPos toBlockPos(MazeRoom room)
    {
        if (room.getDimensions() != 3) throw new IllegalArgumentException();
        return new BlockPos(room.getCoordinate(0), room.getCoordinate(1), room.getCoordinate(2));
    }

    public BlockPattern copy()
    {
        return new BlockPattern(pattern.copy(), ingredients.stream().map(Ingredient::copy).collect(Collectors.toList()));
    }

    public void transform(AxisAlignedTransform2D transform, int[] size)
    {
        pattern.transform(transform, size);
    }

    @Nonnull
    protected BlockPattern copy(AxisAlignedTransform2D transform, int[] size)
    {
        BlockPattern transformed = copy();
        transformed.transform(transform, size);
        return transformed;
    }

    public Optional<Ingredient> findIngredient(String id)
    {
        return ingredients.stream().filter(i -> i.identifier.equals(id)).findFirst();
    }

    public boolean test(World world, BlockPos pos)
    {
        return pattern.compile(true).entrySet().stream().allMatch(
                entry -> findIngredient(entry.getValue()).filter(i -> i.matcher.test(world.getBlockState(toBlockPos(entry.getKey()).add(pos)))).isPresent()
        );
    }

    public boolean canPlace(World world, BlockPos pos, int[] size, boolean rotate, boolean mirror)
    {
        return Transforms.transformStream(rotate, mirror)
                .anyMatch(transform -> copy(transform, size).testAll(world, pos).findAny().isPresent());
    }

    public Multimap<AxisAlignedTransform2D, BlockPos> testAll(World world, BlockPos pos, int[] size, boolean rotate, boolean mirror)
    {
        return Transforms.transformStream(rotate, mirror)
                .collect(GuavaCollectors.toMultimap(o -> o, transform -> copy(transform, size).testAll(world, pos)::iterator));
    }

    @Nonnull
    public Stream<BlockPos> testAll(World world, BlockPos pos)
    {
        return pattern.compile(true).keySet().stream()
                .map((room) -> pos.subtract(toBlockPos(room)))
                .filter(p -> test(world, p));
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        pattern.readFromNBT(compound.getCompoundTag("pattern"));

        ingredients.clear();
        ingredients.addAll(NBTCompoundObjects.readListFrom(compound, "ingredients", Ingredient.class));
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        NBTCompoundObjects.writeTo(compound, "pattern", pattern);

        NBTCompoundObjects.writeListTo(compound, "ingredients", ingredients);
    }

    public void setToAir(World world, BlockPos pos, AxisAlignedTransform2D transform, int[] size)
    {
        BlockPattern transformed = copy(transform, size);
        transformed.setToAir(world, pos);
    }

    public void setToAir(World world, BlockPos pos)
    {
        pattern.compile(true).entrySet().stream()
                .filter(entry -> findIngredient(entry.getValue()).filter(i -> i.delete).isPresent())
                .forEach(entry -> world.setBlockToAir(toBlockPos(entry.getKey()).add(pos)));
    }

    public static class Ingredient implements NBTCompoundObject
    {
        public String identifier;
        public BlockMatcher matcher;
        public boolean delete;

        public Ingredient()
        {
            identifier = "";
            matcher = new BlockMatcher(RecurrentComplex.specialRegistry, "");
            delete = true;
        }

        public Ingredient(String identifier, BlockMatcher matcher, boolean delete)
        {
            this.identifier = identifier;
            this.matcher = matcher;
            this.delete = delete;
        }

        @Override
        public void readFromNBT(NBTTagCompound compound)
        {
            identifier = compound.getString("identifier");
            matcher = new BlockMatcher(RecurrentComplex.specialRegistry, compound.getString("blockExpression"));
            delete = compound.getBoolean("delete");
        }

        @Override
        public void writeToNBT(NBTTagCompound compound)
        {
            compound.setString("identifier", identifier);
            compound.setString("blockExpression", matcher.getExpression());
            compound.setBoolean("delete", delete);
        }

        public Ingredient copy()
        {
            return new Ingredient(identifier, new BlockMatcher(matcher.registry, matcher.getExpression()), delete);
        }
    }

    public static class Serializer implements JsonSerializer<BlockPattern>, JsonDeserializer<BlockPattern>
    {
        @Override
        public BlockPattern deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.asJsonObject(json, "blockPattern");

            List<Selection.Area> pattern = context.deserialize(jsonObject.get("pattern"), new TypeToken<List<Selection.Area>>(){}.getType());
            List<Ingredient> ingredients = gson.fromJson(jsonObject.get("ingredients"), new TypeToken<List<Ingredient>>(){}.getType());

            return new BlockPattern(pattern, ingredients);
        }

        @Override
        public JsonElement serialize(BlockPattern src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.add("pattern", gson.toJsonTree(src.pattern));
            jsonObject.add("ingredients", gson.toJsonTree(src.ingredients));

            return jsonObject;
        }
    }

    public static class IngredientSerializer implements JsonSerializer<Ingredient>, JsonDeserializer<Ingredient>
    {
        private MCRegistry registry;

        public IngredientSerializer(MCRegistry registry)
        {
            this.registry = registry;
        }

        @Override
        public Ingredient deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.asJsonObject(json, "blockPatternIngredient");

            String id = JsonUtils.getString(jsonObject, "id", "");
            String blockExpression = JsonUtils.getString(jsonObject, "blockExpression", "");
            boolean delete = JsonUtils.getBoolean(jsonObject, "delete", true);

            return new Ingredient(id, new BlockMatcher(registry, blockExpression), delete);
        }

        @Override
        public JsonElement serialize(Ingredient src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", src.identifier);
            jsonObject.addProperty("blockExpression", src.matcher.getExpression());
            jsonObject.addProperty("delete", src.delete);

            return jsonObject;
        }
    }
}
