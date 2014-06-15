package ivorius.structuregen.gui.table;

/**
 * Created by lukas on 02.06.14.
 */
public class Bounds
{
    private int minX, maxX;
    private int minY, maxY;

    public Bounds(int minX, int maxX, int minY, int maxY)
    {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    public static Bounds boundsWithSize(int x, int width, int y, int height)
    {
        return new Bounds(x, x + width, y, y + height);
    }

    public int getMinX()
    {
        return minX;
    }

    public int getMaxX()
    {
        return maxX;
    }

    public int getMinY()
    {
        return minY;
    }

    public int getMaxY()
    {
        return maxY;
    }

    public int getWidth()
    {
        return maxX - minX;
    }

    public int getHeight()
    {
        return maxY - minY;
    }

    public int getCenterX()
    {
        return (minX + maxX) / 2;
    }

    public int getCenterY()
    {
        return (minY + maxY) / 2;
    }
}
