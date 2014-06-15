package ivorius.structuregen.worldgen.blockTransformers;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import ivorius.structuregen.gui.table.TableDataSource;

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
