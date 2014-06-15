package ivorius.structuregen.ivtoolkit;

/**
 * Created by lukas on 09.06.14.
 */
public class IvShapeHelper
{
    public static boolean isPointInSpheroid(double[] point, double[] spheroidOrigin, double[] spheroidRadius)
    {
        double totalDist = 0.0;

        for (int i = 0; i < point.length; i++)
        {
            double dist = (point[i] - spheroidOrigin[i]) / spheroidRadius[i];
            totalDist += dist * dist;
        }

        return totalDist < 1.0;
    }
}
