package ivorius.structuregen.gui.table;

/**
 * Created by lukas on 03.06.14.
 */
public interface TableElementProperty<P> extends TableElement
{
    P getPropertyValue();

    void setPropertyValue(P value);
}
