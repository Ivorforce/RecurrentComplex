package ivorius.structuregen.ivtoolkit;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lukas on 12.06.14.
 */
public class IvGsonHelper
{
    public static String serializedName(Enum anEnum)
    {
        String name = anEnum.name();

        try
        {
            SerializedName serializedName = anEnum.getClass().getField(name).getAnnotation(SerializedName.class);

            if (serializedName != null)
            {
                name = serializedName.value();
            }
        }
        catch (NoSuchFieldException ignored)
        {

        }

        return name;
    }

    public static <E extends Enum> E enumForName(String serializedName, E[] values)
    {
        for (E anEnum : values)
        {
            if (serializedName(anEnum).equals(serializedName))
                return anEnum;
        }

        return null;
    }
}
