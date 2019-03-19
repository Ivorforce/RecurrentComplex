/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex;

import com.google.common.collect.Lists;
import gnu.trove.map.TObjectFloatMap;
import ivorius.reccomplex.utils.algebra.ExpressionCache;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ConfigUtil
{
    public static <K, V> void parseMap(String[] strings, String keyName, Function<String, K> keyFunc, String valueName, Function<String, V> valueFunc, BiConsumer<K, V> map)
    {
        for (String string : strings)
            parseMap(string, (ks, vs) ->
            {
                K key = keyFunc.apply(ks.trim());
                if (key == null)
                {
                    RecurrentComplex.logger.error("Failed parsing " + keyName + " key ''" + ks + "' for configuration");
                    return;
                }

                V val = valueFunc.apply(vs.trim());
                if (val == null)
                {
                    RecurrentComplex.logger.error("Failed parsing " + valueName + " value ''" + vs + "' for configuration");
                    return;
                }

                map.accept(key, val);
            });
    }

    public static void parseMap(String[] strings, BiConsumer<String, String> consumer)
    {
        for (String string : strings)
            parseMap(string, consumer);
    }

    public static void parseMap(String string, BiConsumer<String, String> consumer)
    {
        String[] parts = string.split(":", 2);
        if (parts.length == 2)
            consumer.accept(parts[0], parts[1]);
        else
            RecurrentComplex.logger.error("Failed finding key (separated by ':') in ''" + string + "'");
    }

    public static void logExpressionException(ExpressionCache<?> cache, String name, Logger logger)
    {
        if (cache.getParseException() != null)
            logger.error("Error in expression '" + name + "'", cache.getParseException());
    }

    public static List<String> writeMap(TObjectFloatMap<String> map) {
        ArrayList<String> strings = Lists.newArrayList();

        map.forEachEntry((key, val) -> {
            strings.add(String.format("%s:%s", key, val));

            return true;
        });

        return strings;
    }
}
