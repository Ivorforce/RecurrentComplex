/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

import gnu.trove.map.hash.TIntObjectHashMap;
import ivorius.ivtoolkit.math.IvMathHelper;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Mouse;

import java.util.*;

/**
 * Created by lukas on 30.05.14.
 */
public class GuiTable extends Gui
{
    private static final ResourceLocation CREATIVE_INVENTORY_TABS = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");

    public static final int TITLE_HEIGHT = 25;
    public static final int HEIGHT_PER_SLOT = 22;
    public static final int SCROLL_BAR_WIDTH = 19;
    public static final int SCROLL_BAR_MARGIN = 4;
    public static final float SCROLL_SPEED = 0.005f;

    private TableDelegate delegate;
    private TableDataSource dataSource;

    private Bounds bounds;
    private Bounds tableBounds;

    private boolean allowsNegativeScroll = false;
    private boolean startCentered = true;
    private boolean firstTime = true;
    private boolean showsScrollBar;
    private boolean dragsScrollBar;
    private float currentScroll;

    private boolean hideScrollbarIfUnnecessary;

    private final TIntObjectHashMap<TableCell> cachedCells = new TIntObjectHashMap<>();
    private final List<TableCell> currentCells = new ArrayList<>();
    private final Set<String> lockedCells = new HashSet<>();

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

    public boolean isAllowsNegativeScroll()
    {
        return allowsNegativeScroll;
    }

    public void setAllowsNegativeScroll(boolean allowsNegativeScroll)
    {
        this.allowsNegativeScroll = allowsNegativeScroll;
    }

    public void initGui()
    {
        buttonMap.clear();

        for (TableCell cell : currentCells)
            cell.setHidden(true);
        currentCells.clear();

        ////////

        int numberOfCells = dataSource.numberOfCells();
        int supportedSlotNumber = tableBounds.getHeight() / HEIGHT_PER_SLOT;

        if (firstTime)
        {
            if (supportedSlotNumber > numberOfCells && allowsNegativeScroll && startCentered)
                currentScroll = (getMinScroll() + getMaxScroll()) / 2; // Scroll to the middle
            firstTime = false;
        }
        else
            updateScrollUpwards(0); // If we're too far down we scroll up now

        int roundedScrollIndex = MathHelper.floor(currentScroll + 0.5f);

        scrollUpButton = new GuiButton(-1, tableBounds.getMaxX() + SCROLL_BAR_MARGIN, tableBounds.getMinY(), SCROLL_BAR_WIDTH - SCROLL_BAR_MARGIN, 20, TextFormatting.BOLD + "↑");
        delegate.addButtonToTable(scrollUpButton);
        scrollDownButton = new GuiButton(-1, tableBounds.getMaxX() + SCROLL_BAR_MARGIN, tableBounds.getMaxY() - 20, SCROLL_BAR_WIDTH - SCROLL_BAR_MARGIN, 20, TextFormatting.BOLD + "↓");
        delegate.addButtonToTable(scrollDownButton);

        boolean needsUpScroll = canScrollUp(numberOfCells);
        boolean needsDownScroll = canScrollDown(numberOfCells);

        scrollUpButton.enabled = needsUpScroll;
        scrollDownButton.enabled = needsDownScroll;
        showsScrollBar = needsUpScroll || needsDownScroll || !hideScrollbarIfUnnecessary;

        scrollUpButton.visible = showsScrollBar;
        scrollDownButton.visible = showsScrollBar;

        int baseY = tableBounds.getMinY() + (tableBounds.getHeight()
                - (!allowsNegativeScroll && startCentered ? Math.min(numberOfCells, supportedSlotNumber) : supportedSlotNumber) * HEIGHT_PER_SLOT) / 2;
        for (int index = 0; index < supportedSlotNumber && roundedScrollIndex + index < numberOfCells; index++)
        {
            int cellIndex = roundedScrollIndex + index;
            if (cellIndex < 0) continue;

            TableCell cell = cachedCells.get(cellIndex);
            boolean initCell = cell == null;

            if (initCell)
                cell = dataSource.cellForIndex(this, cellIndex);

            if (cell == null)
                throw new NullPointerException("Cell not initialized: at " + cellIndex);

            int cellY = index * HEIGHT_PER_SLOT + 1;

            cell.setBounds(Bounds.fromAxes(tableBounds.getMinX(), tableBounds.getWidth(), baseY + cellY, HEIGHT_PER_SLOT - 2));
            cell.setHidden(false);
            cell.initGui(this);

            if (initCell)
                cachedCells.put(cellIndex, cell);

            currentCells.add(cell);
        }
    }

    public void drawScreen(GuiScreen screen, int mouseX, int mouseY, float partialTicks)
    {
        GlStateManager.color(1, 1, 1);
        GlStateManager.scale(2, 2, 2);
        drawString(Minecraft.getMinecraft().fontRendererObj, dataSource.title(), bounds.getMinX() / 2, bounds.getMinY() / 2 + 2, 0xffffffff);
        GlStateManager.scale(0.5f, 0.5f, 0.5f);

        currentCells.stream().filter(cell -> !cell.isHidden()).forEach(cell -> cell.draw(this, mouseX, mouseY, partialTicks));
        currentCells.stream().filter(cell -> !cell.isHidden()).forEach(cell -> cell.drawFloating(this, mouseX, mouseY, partialTicks));

        if (showsScrollBar && getMaxScroll() != getMinScroll())
        {
            screen.mc.getTextureManager().bindTexture(CREATIVE_INVENTORY_TABS);
            GlStateManager.color(1, 1, 1);

            this.drawTexturedModalRect(tableBounds.getMaxX() + SCROLL_BAR_MARGIN + 1, tableBounds.getMinY() + 20 + (this.currentScroll - getMinScroll()) / (getMaxScroll() - getMinScroll()) * (tableBounds.getHeight() - 40 - 15), 232, 0, 12, 15);
        }
    }

    public void updateScreen()
    {
        currentCells.forEach(cell -> cell.update(this));
    }

    public void actionPerformed(GuiButton button)
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

    public boolean keyTyped(char keyChar, int keyCode)
    {
        for (TableCell cell : currentCells)
        {
            if (cell.keyTyped(keyChar, keyCode))
            {
                return true;
            }
        }

        return false;
    }

    public void mouseReleased(int x, int y, int button)
    {
        dragsScrollBar = false;
    }

    public void mouseClicked(int x, int y, int button)
    {
        for (TableCell cell : currentCells)
            cell.mouseClicked(button, x, y);

        if (scrollBarBounds().contains(x, y))
        {
            dragsScrollBar = true;
            dragScrollBar(x, y);
        }
    }

    public void mouseClickMove(int x, int y, int button, long timeSinceLastClick)
    {
        if (dragsScrollBar)
            dragScrollBar(x, y);
    }

    public void dragScrollBar(int x, int y)
    {
        Bounds scrollBarBounds = scrollBarBounds();

        float scrollHeight = (float) (y - scrollBarBounds.getMinY()) / (float) scrollBarBounds.getHeight();
        setScroll(getMinScroll() + scrollHeight * (getMaxScroll() - getMinScroll()));
        delegate.redrawTable();
    }

    public void addButton(TableCell property, int id, GuiButton button)
    {
        delegate.addButtonToTable(button);

        buttonMap.put(button, new ImmutablePair<>(property, id));
    }

    public Bounds getTableBounds()
    {
        return tableBounds;
    }

    public Bounds getBounds()
    {
        return bounds;
    }

    public void setBounds(Bounds bounds)
    {
        this.bounds = bounds;
        this.tableBounds = Bounds.fromSize(bounds.getMinX(), bounds.getMinY() + TITLE_HEIGHT, bounds.getWidth() - SCROLL_BAR_WIDTH, bounds.getHeight() - TITLE_HEIGHT);
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
        return getMinScroll(dataSource.numberOfCells());
    }

    public float getMinScroll(int numberOfCells)
    {
        if (hideScrollbarIfUnnecessary || !allowsNegativeScroll)
            return 0;

        int supportedSlots = tableBounds.getHeight() / HEIGHT_PER_SLOT;
        return Math.min(0, numberOfCells - supportedSlots);
    }

    public float getMaxScroll()
    {
        return getMaxScroll(dataSource.numberOfCells());
    }

    protected float getMaxScroll(int numberOfCells)
    {
        int supportedSlots = tableBounds.getHeight() / HEIGHT_PER_SLOT;
        return Math.max(0, numberOfCells - supportedSlots);
    }

    public void tryScrollUp(float dist)
    {
        updateScrollUpwards(dist);
        delegate.redrawTable();
    }

    protected void updateScrollUpwards(float dist)
    {
        setScroll(currentScroll - dist);
    }

    private void setScroll(float value)
    {
        currentScroll = IvMathHelper.clamp(getMinScroll(), value, getMaxScroll());
    }

    public boolean canScrollUp(int numberOfCells)
    {
        return currentScroll > getMinScroll(numberOfCells);
    }

    public boolean canScrollDown()
    {
        return canScrollDown(dataSource.numberOfCells());
    }

    protected boolean canScrollDown(int numberOfCells)
    {
        return currentScroll < getMaxScroll(numberOfCells);
    }

    public Bounds scrollBarBounds()
    {
        return Bounds.fromSize(tableBounds.getMaxX() + SCROLL_BAR_MARGIN, tableBounds.getMinY() + 20, SCROLL_BAR_WIDTH - SCROLL_BAR_MARGIN, tableBounds.getHeight() - 40);
    }

    public void clearCellCache()
    {
        cachedCells.retainEntries((key, cell) -> lockedCells.contains(cell.getID()));
    }

    public void setLocked(String cell, boolean lock)
    {
        if (lock)
            lockedCells.add(cell);
        else
            lockedCells.remove(cell);
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

            if (j2 + k > this.bounds.getWidth())
            {
                j2 -= 28 + k;
            }

            if (k2 + i1 + 6 > this.bounds.getHeight())
            {
                k2 = this.bounds.getHeight() - i1 - 6;
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
