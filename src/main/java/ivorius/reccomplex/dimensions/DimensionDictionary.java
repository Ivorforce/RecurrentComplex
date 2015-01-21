/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

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
     * Dimensions that are on the surface on a planet.
     */
    public static final String OVERWORLD = "OVERWORLD";
    /**
     * Dimensions that are above the surface of a planet.
     */
    public static final String SKY = "SKY";
    /**
     * Dimensions that are below the surface of a planet.
     */
    public static final String UNDERGROUND = "UNDERGROUND";
    /**
     * Dimensions that are in free space.
     */
    public static final String SPACE = "SPACE";
    /**
     * Dimensions that are deemed to be on the surface of our earth (like the default dimension).
     */
    public static final String EARTH = "EARTH";
    /**
     * Dimensions that are 'hell' based - e.g. netherrack, lava, fire.
     */
    public static final String HELL = "HELL";
    /**
     * Dimensions that are 'Ender' based - e.g. Endstone, Endermen
     */
    public static final String ENDER = "ENDER";
    /**
     * Dimensions that are made up of underground caves (like the overworld's underground area).
     */
    public static final String CAVES = "CAVES";

    private static final TIntObjectMap<Set<String>> dimensionTypes = new TIntObjectHashMap<>();
    private static final Map<String, Type> types = new HashMap<>();

    static
    {
        registerSubtypes(UNREAL, Arrays.asList(IMAGINARY, SIMULATED, ABSTRACT));

        registerSubtypes(OVERWORLD, Arrays.asList(EARTH));
        registerSubtypes(SKY, Arrays.asList(ENDER));
        registerSubtypes(UNDERGROUND, Arrays.asList(HELL, CAVES));

        registerType(SPACE);

        registerDimensionTypes(0, Arrays.asList(EARTH, REAL, MC_DEFAULT));
        registerDimensionTypes(-1, Arrays.asList(HELL, REAL, MC_DEFAULT));
        registerDimensionTypes(1, Arrays.asList(ENDER, REAL, MC_DEFAULT));
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
            dTypes = new HashSet<>();
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
     * @param subTypes The subtypes.
     */
    public static void registerSubtypes(String type, Collection<String> subTypes)
    {
        registerGetType(type).subtypes.addAll(subTypes);

        for (String sub : subTypes)
            registerGetType(sub).supertypes.add(type);
    }

    /**
     * Adds the specified type as a subtype to each of the collection's type.
     * This will also register any used types, if they weren't already.
     *
     * @param type     The type.
     * @param subTypes The supertypes.
     */
    public static void registerSupertypes(String type, Collection<String> subTypes)
    {
        registerGetType(type).supertypes.addAll(subTypes);

        for (String sub : subTypes)
            registerGetType(sub).subtypes.add(type);
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
        return types != null ? Collections.unmodifiableSet(types) : Collections.<String>emptySet();
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

        Queue<String> curTypes = new ArrayDeque<>();
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
        public final Set<String> supertypes = new HashSet<>();
        public final Set<String> subtypes = new HashSet<>();
    }
}
