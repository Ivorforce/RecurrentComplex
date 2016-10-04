/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.maze;

import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lukas on 26.04.15.
 */
public class ConnectorFactory
{
    public static final String MALE_PREFIX = "Male:";
    public static final String FEMALE_PREFIX = "Female:";

    private final Map<String, Connector> connectors = new HashMap<>();

    public Connector get(String id)
    {
        Connector connector = connectors.get(id);
        return connector != null ? connector : register(id);
    }

    public Connector register(String id)
    {
        Connector gendered = tryCreateGendered(id);

        if (gendered != null)
            return gendered;
        else
        {
            SimpleConnectors.Hermaphrodite hermaphrodite = new SimpleConnectors.Hermaphrodite(id);
            connectors.put(id, hermaphrodite);
            return hermaphrodite;
        }
    }

    public void put(String id, Connector connector)
    {
        connectors.put(id, connector);
    }

    protected Connector tryCreateGendered(String id)
    {
        String baseID;

        baseID = fromPrefix(MALE_PREFIX, id);
        if (baseID != null)
            return createGendered(true, baseID);

        baseID = fromPrefix(FEMALE_PREFIX, id);
        if (baseID != null)
            return createGendered(false, baseID);

        return null;
    }

    protected Connector createGendered(boolean returnMale, String baseID)
    {
        String male = MALE_PREFIX + baseID;
        String female = FEMALE_PREFIX + baseID;

        Pair<SimpleConnectors.Gendered, SimpleConnectors.Gendered> gendered = SimpleConnectors.Gendered.create(male, female);

        connectors.put(male, gendered.getLeft());
        connectors.put(female, gendered.getRight());

        return returnMale ? gendered.getLeft() : gendered.getRight();
    }

    private static String fromPrefix(String prefix, String id)
    {
        return id.startsWith(prefix) ? id.substring(prefix.length()) : null;
    }
}
