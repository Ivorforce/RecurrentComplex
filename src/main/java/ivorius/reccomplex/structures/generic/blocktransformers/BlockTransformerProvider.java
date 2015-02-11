/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.blocktransformers;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import ivorius.reccomplex.gui.table.TableDataSource;

/**
 * Created by lukas on 05.06.14.
 */
public interface BlockTransformerProvider<T extends BlockTransformer>
{
    T defaultTransformer();

    TableDataSource tableDataSource(T element);

    JsonSerializer<T> serializer();

    JsonDeserializer<T> deserializer();
}
