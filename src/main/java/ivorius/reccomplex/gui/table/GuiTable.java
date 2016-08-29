/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

import gnu.trove.map.hash.TIntObjectHashMap;
import ivorius.ivtoolkit.math.IvMathHelper;
import ivorius.ivtoolkit.tools.IvTranslations;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Mouse;
import net.minecraft.client.renderer.GlStateManager;

import java.util.*;

/**
 * Created by lukas on 30.05.14.
 */
public class GuiTable extends Gui
{
    public static final int HEIGHT_PER_SLOT = 25;
    public static final int SCROLL_BAR_HEIGHT = 27;
    public static final float SCROLL_SPEED = 0.005f;

    private TableDelegate delegate;
    private TableDataSource dataSource;

    private Bounds propertiesBounds;
    private float currentScroll;
    private int cachedMaxIndex;

    private boolean hideScrollbarIfUnnecessary;

    private final TIntObjectHashMap<TableElement> cachedElements = new TIntObjectHashMap<>();
    private final List<TableElement> currentElements = new ArrayList<>();
    private final Set<String> lockedElements = new HashSet<>();

    private Map<GuiButton, Pair<TableCell, Integer>> buttonMap = new HashMap<>();

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
            element.setHidden(true);
        currentElements.clear();

        ////////

        int roundedScrollIndex = MathHelper.floor_float(currentScroll + 0.5f);

        scrollUpButton = new GuiButton(-1, propertiesBounds.getMinX(), propertiesBounds.getMinY(), propertiesBounds.getWidth() / 2 - 1, 20, IvTranslations.get("gui.up"));
        delegate.addButton(scrollUpButton);
        scrollDownButton = new GuiButton(-1, propertiesBounds.getCenterX() + 1, propertiesBounds.getMinY(), propertiesBounds.getWidth() / 2 - 1, 20, IvTranslations.get("gui.down"));
        delegate.addButton(scrollDownButton);

        int supportedSlotNumber = (propertiesBounds.getHeight() - SCROLL_BAR_HEIGHT) / HEIGHT_PER_SLOT;
        int numberOfElements = dataSource.numberOfElements();
        cachedMaxIndex = roundedScrollIndex + supportedSlotNumber - 1;

        boolean needsUpScroll = canScrollUp();
        boolean needsDownScroll = canScrollDown(numberOfElements);
        boolean needsScroll = needsUpScroll || needsDownScroll;

        scrollUpButton.enabled = needsUpScroll;
        scrollDownButton.enabled = needsDownScroll;
        scrollUpButton.visible = needsScroll || !hideScrollbarIfUnnecessary;
        scrollDownButton.visible = needsScroll || !hideScrollbarIfUnnecessary;

        int baseY = propertiesBounds.getMinY() + SCROLL_BAR_HEIGHT;
        for (int index = 0; index < supportedSlotNumber && roundedScrollIndex + index < numberOfElements; index++)
        {
            TableElement element = cachedElements.get(roundedScrollIndex + index);
            boolean initElement = element == null;

            if (initElement)
                element = dataSource.elementForIndex(this, roundedScrollIndex + index);

            if (element == null)
                throw new NullPointerException("Element not initialized: at " + (roundedScrollIndex + index));

            int elementY = index * HEIGHT_PER_SLOT;

            element.setBounds(Bounds.fromAxes(propertiesBounds.getMinX() + 100, propertiesBounds.getWidth() - 100, baseY + elementY, 20));
            element.setHidden(false);
            element.initGui(this);

            if (initElement)
                cachedElements.put(roundedScrollIndex + index, element);

            currentElements.add(element);
        }
    }

    public void drawScreen(GuiScreen screen, int mouseX, int mouseY, float partialTicks)
    {
        currentElements.stream().filter(element -> !element.isHidden()).forEach(element -> {
            String title = element.getTitle();
            if (title != null)
            {
                Bounds bounds = element.bounds();

                int stringWidth = screen.mc.fontRendererObj.getStringWidth(title);
                screen.drawString(screen.mc.fontRendererObj, title, bounds.getMinX() - stringWidth - 10, bounds.getCenterY() - 4, 0xffffffff);
            }
        });

        currentElements.stream().filter(element -> !element.isHidden()).forEach(element -> element.draw(this, mouseX, mouseY, partialTicks));

        currentElements.stream().filter(element -> !element.isHidden()).forEach(element -> element.drawFloating(this, mouseX, mouseY, partialTicks));
    }

    public void updateScreen()
    {
        currentElements.forEach(tableElement -> tableElement.update(this));
    }

    protected void actionPerformed(GuiButton button)
    {
        if (button == scrollDownButton)
        {
            tryScrollDown();
        }
        else if (button == scrollUpButton)
        {
            tryScrollUp();
        }
        else
        {
            Pair<TableCell, Integer> propertyPair = buttonMap.get(button);
            if (propertyPair != null)
                propertyPair.getLeft().buttonClicked(propertyPair.getRight());
        }
    }

    public void handleMouseInput()
    {
        int i = Mouse.getEventDWheel();

        if (i != 0)
            tryScrollUp(i * SCROLL_SPEED);
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

    public void addButton(TableCell property, int id, GuiButton button)
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

    public void tryScrollUp()
    {
        tryScrollUp(1);
    }

    public void tryScrollDown()
    {
        tryScrollUp(-1);
    }

    public float getMinScroll()
    {
        return 0;
    }

    public float getMaxScroll()
    {
        return getMaxScroll(dataSource.numberOfElements());
    }

    protected float getMaxScroll(int numberOfElements)
    {
        return Math.max(0, numberOfElements - 1 - (cachedMaxIndex - MathHelper.floor_float(currentScroll + 0.5f)));
    }

    public void tryScrollUp(float dist)
    {
        currentScroll = IvMathHelper.clamp(getMinScroll(), currentScroll - dist, getMaxScroll());
        delegate.redrawTable();
    }

    public boolean canScrollUp()
    {
        return currentScroll > getMinScroll();
    }

    public boolean canScrollDown()
    {
        return canScrollDown(dataSource.numberOfElements());
    }

    protected boolean canScrollDown(int numberOfElements)
    {
        return currentScroll < getMaxScroll(numberOfElements);
    }

    public void clearElementCache()
    {
        cachedElements.retainEntries((key, element) -> lockedElements.contains(element.getID()));
    }

    public void setLocked(String element, boolean lock)
    {
        if (lock)
            lockedElements.add(element);
        else
            lockedElements.remove(element);
    }

    // Accessors

    public void drawTooltipRect(List<String> lines, Bounds bounds, int mouseX, int mouseY, FontRenderer font)
    {
        if (bounds.contains(mouseX, mouseY))
            drawTooltip(lines, mouseX, mouseY, font);
    }

    public void drawTooltip(List<String> lines, int x, int y, FontRenderer font)
    {
        if (!lines.isEmpty())
        {
            GlStateManager.disableDepth(); // depthTest
            int k = 0;

            for (String s : lines)
            {
                int l = font.getStringWidth(s);

                if (l > k)
                {
                    k = l;
                }
            }

            int j2 = x + 12;
            int k2 = y - 12;
            int i1 = 8;

            if (lines.size() > 1)
            {
                i1 += 2 + (lines.size() - 1) * 10;
            }

            if (j2 + k > this.propertiesBounds.getWidth())
            {
                j2 -= 28 + k;
            }

            if (k2 + i1 + 6 > this.propertiesBounds.getHeight())
            {
                k2 = this.propertiesBounds.getHeight() - i1 - 6;
            }

            this.zLevel = 300.0F;
            int j1 = -267386864;
            this.drawGradientRect(j2 - 3, k2 - 4, j2 + k + 3, k2 - 3, j1, j1);
            this.drawGradientRect(j2 - 3, k2 + i1 + 3, j2 + k + 3, k2 + i1 + 4, j1, j1);
            this.drawGradientRect(j2 - 3, k2 - 3, j2 + k + 3, k2 + i1 + 3, j1, j1);
            this.drawGradientRect(j2 - 4, k2 - 3, j2 - 3, k2 + i1 + 3, j1, j1);
            this.drawGradientRect(j2 + k + 3, k2 - 3, j2 + k + 4, k2 + i1 + 3, j1, j1);
            int k1 = 1347420415;
            int l1 = (k1 & 16711422) >> 1 | k1 & -16777216;
            this.drawGradientRect(j2 - 3, k2 - 3 + 1, j2 - 3 + 1, k2 + i1 + 3 - 1, k1, l1);
            this.drawGradientRect(j2 + k + 2, k2 - 3 + 1, j2 + k + 3, k2 + i1 + 3 - 1, k1, l1);
            this.drawGradientRect(j2 - 3, k2 - 3, j2 + k + 3, k2 - 3 + 1, k1, k1);
            this.drawGradientRect(j2 - 3, k2 + i1 + 2, j2 + k + 3, k2 + i1 + 3, l1, l1);

            for (int i2 = 0; i2 < lines.size(); ++i2)
            {
                String s1 = lines.get(i2);
                font.drawStringWithShadow(s1, j2, k2, -1);

                if (i2 == 0)
                {
                    k2 += 2;
                }

                k2 += 10;
            }

            this.zLevel = 0.0F;
        }
    }
}
