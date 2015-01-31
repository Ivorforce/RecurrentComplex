package ivorius.reccomplex.dimensions;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.*;

/**
 * Handles tags for dimensions, in a way similar to {@link net.minecraftforge.common.BiomeDictionary}.
 * <p/>
 * A type can have subtypes and supertypes.
 * A dimension matches a type when it is associated with either the type or any of its sub-types.
 */
public class DimensionDictionary
{
    /**
     * Dimensions that are not registered in the dictionary default to this category.
     * Assigning this to your dimension manually is discouraged.
     */
    public static final String UNCATEGORIZED = "UNCATEGORIZED";

    /**
     * Dimensions that exist in the base game of Minecraft.
     */
    public static final String MC_DEFAULT = "MC_DEFAULT";

    /**
     * Dimensions that are deemed to be situated in physical reality.
     */
    public static final String REAL = "REAL";
    /**
     * Dimensions that are not strictly situated in physical reality.
     */
    public static final String UNREAL = "UNREAL";
    /**
     * Dimensions made up in the mind of an entity or player. (e.g. his mind)
     */
    public static final String IMAGINARY = "IMAGINARY";
    /**
     * Dimensions that exists inside a digital space (e.g. computer)
     */
    public static final String SIMULATED = "SIMULATED";
    /**
     * Dimensions that are abstract and don't really exist at all.
     */
    public static final String ABSTRACT = "ABSTRACT";

    /**
     * Dimensions with a bottom, but no top limitation, e.g. the surface of a planet.
     */
    public static final String SURFACE = "SURFACE";
    /**
     * Dimensions with a bottom and top limitation, e.g. an underground caves system (nether).
     */
    public static final String CAVE_WORLD = "CAVE_WORLD";
    /**
     * Dimensions with neither bottom nor top limitations, e.g. floating islands or space.
     */
    public static final String FLOATING = "FLOATING";
    /**
     * Dimensions with no bottom, but a top limitation, e.g. an inverted surface world.
     */
    public static final String SURFACE_INVERTED = "SURFACE_INVERTED";

    /**
     * Dimensions that are only defined in a limited space, e.g. dungeon instances or boss arenas.
     */
    public static final String FINITE = "FINITE";
    /**
     * Dimensions that are not limited in space, e.g. any dynamically generated world.
     */
    public static final String INFINITE = "INFINITE";

    /**
     * Dimensions that are deemed to be on the surface of a planet.
     */
    public static final String PLANET_SURFACE = "PLANET_SURFACE";

    /**
     * Dimensions that are 'earth' themed, e.g. the overworld.
     */
    public static final String EARTH = "EARTH";
    /**
     * Dimensions that are 'hell' themed - e.g. netherrack, lava, fire.
     */
    public static final String HELL = "HELL";
    /**
     * Dimensions that are 'ender' themed - e.g. Endstone, Endermen
     */
    public static final String ENDER = "ENDER";
    /**
     * Dimensions intended for a particular boss fight, e.g. the end.
     */
    public static final String BOSS_ARENA = "BOSS_ARENA";

    private static final TIntObjectMap<Set<String>> dimensionTypes = new TIntObjectHashMap<Set<String>>();
    private static final Map<String, Type> types = new HashMap<String, Type>();

    private static final Set<String> SET_UNCATEGORIZED = Collections.singleton(UNCATEGORIZED);

    static
    {
        registerType(UNCATEGORIZED);

        registerSubtypes(UNREAL, Arrays.asList(IMAGINARY, SIMULATED, ABSTRACT));
        registerSubtypes(FINITE, Arrays.asList(BOSS_ARENA));
        registerSubtypes(SURFACE, Arrays.asList(PLANET_SURFACE));

        registerTypes(Arrays.asList(SURFACE_INVERTED));

        registerDimensionTypes(0, Arrays.asList(MC_DEFAULT, REAL, INFINITE, PLANET_SURFACE, EARTH));
        registerDimensionTypes(-1, Arrays.asList(MC_DEFAULT, REAL, INFINITE, CAVE_WORLD, HELL));
        registerDimensionTypes(1, Arrays.asList(MC_DEFAULT, REAL, BOSS_ARENA, FLOATING, ENDER));
    }

    /**
     * Registers a dimension for dimension types.
     * This will also register each of the used types, if they weren't already.
     *
     * @param dimensionID The ID of the dimension.
     * @param types       The dimension types.
     */
    public static void registerDimensionTypes(int dimensionID, Collection<String> types)
    {
        Set<String> dTypes = dimensionTypes.get(dimensionID);
        if (dTypes == null)
        {
            dTypes = new HashSet<String>();
            dimensionTypes.put(dimensionID, dTypes);
        }
        dTypes.addAll(types);

        for (String type : types)
            registerType(type);
    }

    /**
     * Unregisters a dimension from certain dimension types.
     * This is useful for cleanup if a dimension changes type or is destructed.
     *
     * @param dimensionID The ID of the dimension.
     * @param types       The types to unregister, or null to unregister all types.
     */
    public static void unregisterDimensionTypes(int dimensionID, Collection<String> types)
    {
        Set<String> dTypes = dimensionTypes.get(dimensionID);
        if (dTypes != null)
        {
            if (types == null)
                dTypes.clear();
            else
                dTypes.removeAll(types);
        }
    }

    /**
     * Registers a dimension type.
     *
     * @param type The type to register.
     */
    public static void registerType(String type)
    {
        if (!types.containsKey(type))
            types.put(type, new Type());
    }

    /**
     * Registers many dimension types.
     *
     * @param types The types to register.
     */
    public static void registerTypes(Collection<String> types)
    {
        for (String type : types)
            registerType(type);
    }

    /**
     * Adds subtypes to a specific type.
     * This will also register any used types, if they weren't already.
     *
     * @param type     The type.
     * @param subtypes The subtypes.
     */
    public static void registerSubtypes(String type, Collection<String> subtypes)
    {
        registerGetType(type).subtypes.addAll(subtypes);

        for (String sub : subtypes)
            registerGetType(sub).supertypes.add(type);
    }

    /**
     * Adds supertypes to a specific type.
     * This will also register any used types, if they weren't already.
     *
     * @param type     The type.
     * @param supertypes The supertypes.
     */
    public static void registerSupertypes(String type, Collection<String> supertypes)
    {
        registerGetType(type).supertypes.addAll(supertypes);

        for (String supertype : supertypes)
            registerGetType(supertype).subtypes.add(type);
    }

    /**
     * Returns a set of types a specific dimension was registered for.
     *
     * @param dimensionID The dimension's ID.
     * @return The dimension's types.
     */
    public static Set<String> getDimensionTypes(int dimensionID)
    {
        Set<String> types = dimensionTypes.get(dimensionID);
        return types != null ? SET_UNCATEGORIZED : Collections.<String>emptySet();
    }

    /**
     * Determines if a dimension matches all types from a collection of types.
     *
     * @param dimensionID The dimension's ID.
     * @param types       The types.
     * @return True if all types were matched, otherwise false.
     */
    public static boolean dimensionMatchesAllTypes(int dimensionID, Collection<String> types)
    {
        for (String type : types)
        {
            if (!dimensionMatchesType(dimensionID, type))
                return false;
        }

        return true;
    }

    /**
     * Determines if a dimension matches a specific type.
     * This is the case exactly when the dimension is associated with either the type or any of its subtypes.
     *
     * @param dimensionID The dimension's ID.
     * @param type        The type.
     * @return True if the dimension matches the type, otherwise false.
     */
    public static boolean dimensionMatchesType(int dimensionID, String type)
    {
        Set<String> dimTypes = dimensionTypes.get(dimensionID);
        if (dimTypes == null)
            return false;

        Queue<String> curTypes = new ArrayDeque<String>();
        curTypes.add(type);

        String curType;

        while ((curType = curTypes.poll()) != null)
        {
            if (dimTypes.contains(curType))
                return true;

            Type curT = types.get(curType);
            if (curT != null)
                curTypes.addAll(curT.subtypes);
        }

        return false;
    }

    /**
     * Returns a type's supertypes.
     *
     * @param type The type.
     * @return The type's supertypes.
     */
    public static Set<String> getSupertypes(String type)
    {
        Type t = types.get(type);
        return t == null ? Collections.<String>emptySet() : Collections.unmodifiableSet(t.supertypes);
    }

    /**
     * Returns a type's subtypes.
     *
     * @param type The type.
     * @return The type's subtypes.
     */
    public static Set<String> getSubtypes(String type)
    {
        Type t = types.get(type);
        return t == null ? Collections.<String>emptySet() : Collections.unmodifiableSet(t.subtypes);
    }

    private static Type registerGetType(String type)
    {
        registerType(type);
        return types.get(type);
    }

    private static class Type
    {
        public final Set<String> supertypes = new HashSet<String>();
        public final Set<String> subtypes = new HashSet<String>();
    }
}