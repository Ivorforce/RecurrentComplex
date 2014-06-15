package ivorius.structuregen.gui.table;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lukas on 30.05.14.
 */
public class GuiTable
{
    public static final int HEIGHT_PER_SLOT = 25;
    public static final int SCROLL_BAR_HEIGHT = 27;

    private TableDelegate delegate;
    private TableDataSource dataSource;

    private Bounds propertiesBounds;
    private int currentScrollIndex;
    private int cachedMaxIndex;

    private boolean hideScrollbarIfUnnecessary;

    private List<TableElement> currentElements = new ArrayList<>();

    private Map<GuiButton, Pair<TableElement, Integer>> buttonMap = new HashMap<>();

    private GuiButton scrollUpButton;
    private GuiButton scrollDownButton;

    public GuiTable(TableDelegate delegate, TableDataSource dataSource)
    {
        this.delegate = delegate;
        this.dataSource = dataSource;
    }

    public TableDelegate getDelegate()
    {
        return delegate;
    }

    public void setDelegate(TableDelegate delegate)
    {
        this.delegate = delegate;
    }

    public TableDataSource getDataSource()
    {
        return dataSource;
    }

    public void setDataSource(TableDataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public boolean hidesScrollbarIfUnnecessary()
    {
        return hideScrollbarIfUnnecessary;
    }

    public void setHideScrollbarIfUnnecessary(boolean hideScrollbarIfUnnecessary)
    {
        this.hideScrollbarIfUnnecessary = hideScrollbarIfUnnecessary;
    }

    public void initGui()
    {
        buttonMap.clear();

        for (TableElement element : currentElements)
        {
            element.setHidden(true);
        }
        currentElements.clear();

        ////////

        scrollUpButton = new GuiButton(-1, propertiesBounds.getMinX(), propertiesBounds.getMinY(), propertiesBounds.getWidth() / 2 - 1, 20, "Up");
        delegate.addButton(scrollUpButton);
        scrollDownButton = new GuiButton(-1, propertiesBounds.getCenterX() + 1, propertiesBounds.getMinY(), propertiesBounds.getWidth() / 2 - 1, 20, "Down");
        delegate.addButton(scrollDownButton);

        int supportedSlotNumber = (propertiesBounds.getHeight() - SCROLL_BAR_HEIGHT) / HEIGHT_PER_SLOT;
        cachedMaxIndex = currentScrollIndex + supportedSlotNumber - 1;

        boolean needsUpScroll = dataSource.has(this, currentScrollIndex - 1);
        boolean needsDownScroll = dataSource.has(this, cachedMaxIndex + 1);
        boolean needsScroll = needsUpScroll || needsDownScroll;

        scrollUpButton.enabled = needsUpScroll;
        scrollDownButton.enabled = needsDownScroll;
        scrollUpButton.visible = needsScroll || !hideScrollbarIfUnnecessary;
        scrollDownButton.visible = needsScroll || !hideScrollbarIfUnnecessary;

        int baseY = propertiesBounds.getMinY() + SCROLL_BAR_HEIGHT;
        for (int index = 0; index < supportedSlotNumber && dataSource.has(this, currentScrollIndex + index); index++)
        {
            int elementY = index * HEIGHT_PER_SLOT;

            TableElement element = dataSource.elementForIndex(this, currentScrollIndex + index);
            element.setBounds(Bounds.boundsWithSize(propertiesBounds.getMinX() + 100, propertiesBounds.getWidth() - 100, baseY + elementY, 20));
            element.setHidden(false);
            element.initGui(this);

            currentElements.add(element);
        }
    }

    public void drawScreen(GuiScreen screen, int mouseX, int mouseY, float partialTicks)
    {
        for (TableElement element : currentElements)
        {
            if (!element.isHidden())
            {
                element.draw(this, mouseX, mouseY, partialTicks);

                String title = element.getTitle();
                Bounds bounds = element.bounds();

                int stringWidth = screen.mc.fontRenderer.getStringWidth(title);
                screen.drawString(screen.mc.fontRenderer, title, bounds.getMinX() - stringWidth - 10, bounds.getCenterY() - 4, 0xffffffff);
            }
        }
    }

    public void updateScreen()
    {
        for (TableElement element : currentElements)
        {
            element.update(this);
        }
    }

    protected void actionPerformed(GuiButton button)
    {
        if (button == scrollDownButton)
        {
            scrollDownIfPossible();
        }
        else if (button == scrollUpButton)
        {
            scrollUpIfPossible();
        }
        else
        {
            Pair<TableElement, Integer> propertyPair = buttonMap.get(button);
            if (propertyPair != null)
            {
                propertyPair.getLeft().buttonClicked(propertyPair.getRight());
            }
        }
    }

    protected boolean keyTyped(char keyChar, int keyCode)
    {
        for (TableElement element : currentElements)
        {
            if (element.keyTyped(keyChar, keyCode))
            {
                return true;
            }
        }

        return false;
    }

    protected void mouseClicked(int x, int y, int button)
    {
        for (TableElement element : currentElements)
        {
            element.mouseClicked(button, x, y);
        }
    }

    public void setValueForProperty(String id, Object value)
    {
        for (TableElement element : currentElements)
        {
            if (element instanceof TableElementProperty && element.getID().equals(id))
            {
                ((TableElementProperty) element).setPropertyValue(value);
            }
        }

        throw new IllegalArgumentException("Unknown id: " + id);
    }

    public <V> V valueForProperty(String id)
    {
        for (TableElement element : currentElements)
        {
            if (element instanceof TableElementProperty && element.getID().equals(id))
            {
                return (V) ((TableElementProperty) element).getPropertyValue();
            }
        }

        throw new IllegalArgumentException("Unknown id: " + id);
    }

    public void addButton(TableElement property, int id, GuiButton button)
    {
        delegate.addButton(button);

        buttonMap.put(button, new ImmutablePair<>(property, id));
    }

    public Bounds getPropertiesBounds()
    {
        return propertiesBounds;
    }

    public void setPropertiesBounds(Bounds propertiesBounds)
    {
        this.propertiesBounds = propertiesBounds;
    }

    public void scrollUpIfPossible()
    {
        if (dataSource.has(this, currentScrollIndex - 1))
        {
            currentScrollIndex--;
            delegate.reloadData();
        }
    }

    public void scrollDownIfPossible()
    {
        if (dataSource.has(this, cachedMaxIndex + 1))
        {
            currentScrollIndex++;
            delegate.reloadData();
        }
    }
}
