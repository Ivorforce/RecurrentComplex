package ivorius.structuregen.gui.table;

/**
 * Created by lukas on 30.05.14.
 */
public interface TableElement
{
    String getTitle();

    String getID();

    void initGui(GuiTable screen);

    void setBounds(Bounds bounds);

    Bounds bounds();

    void setHidden(boolean hidden);

    boolean isHidden();

    void draw(GuiTable screen, int mouseX, int mouseY, float partialTicks);

    void update(GuiTable screen);

    boolean keyTyped(char keyChar, int keyCode);

    void mouseClicked(int button, int x, int y);

    void buttonClicked(int buttonID);
}
