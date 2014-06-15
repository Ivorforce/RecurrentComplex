package ivorius.structuregen.gui.table;

/**
 * Created by lukas on 02.06.14.
 */
public abstract class TableElementDefault implements TableElement
{
    protected String title;
    protected String id;

    private boolean hidden = false;

    private Bounds bounds = new Bounds(0, 0, 0, 0);

    public TableElementDefault(String id, String title)
    {
        this.id = id;
        this.title = title;
    }

    @Override
    public String getID()
    {
        return id;
    }

    @Override
    public String getTitle()
    {
        return title;
    }

    @Override
    public void initGui(GuiTable screen)
    {

    }

    @Override
    public void setBounds(Bounds bounds)
    {
        this.bounds = bounds;
    }

    @Override
    public Bounds bounds()
    {
        return bounds;
    }

    @Override
    public void setHidden(boolean hidden)
    {
        this.hidden = hidden;
    }

    @Override
    public boolean isHidden()
    {
        return hidden;
    }

    @Override
    public void draw(GuiTable screen, int mouseX, int mouseY, float partialTicks)
    {

    }

    @Override
    public void update(GuiTable screen)
    {

    }

    @Override
    public boolean keyTyped(char keyChar, int keyCode)
    {
        return false;
    }

    @Override
    public void mouseClicked(int button, int x, int y)
    {

    }

    @Override
    public void buttonClicked(int buttonID)
    {

    }
}
