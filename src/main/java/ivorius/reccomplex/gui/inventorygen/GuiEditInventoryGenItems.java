/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.inventorygen;

import ivorius.ivtoolkit.gui.*;
import ivorius.ivtoolkit.network.PacketGuiAction;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.GuiHider;
import ivorius.reccomplex.gui.InventoryWatcher;
import ivorius.reccomplex.gui.RCGuiHandler;
import ivorius.reccomplex.utils.SaveDirectoryData;
import ivorius.reccomplex.world.storage.loot.GenericItemCollection;
import ivorius.reccomplex.world.storage.loot.GenericItemCollection.Component;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lukas on 26.05.14.
 */
public class GuiEditInventoryGenItems extends GuiContainer implements InventoryWatcher
{
    public static ResourceLocation textureBackground = new ResourceLocation(RecurrentComplex.MOD_ID, RecurrentComplex.filePathTextures + "gui_edit_inventory_gen.png");

    public String key;
    public Component component;
    public SaveDirectoryData saveDirectoryData;

    private GuiButton backBtn;

    private GuiButton nextPageBtn;
    private GuiButton prevPageBtn;

    private List<GuiSlider> weightSliders = new ArrayList<>();
    private List<GuiSliderRange> minMaxSliders = new ArrayList<>();

    private int currentColShift;

    public GuiEditInventoryGenItems(EntityPlayer player, GenericItemCollection.Component component, String key, SaveDirectoryData saveDirectoryData)
    {
        super(new ContainerEditInventoryGenItems(player, key, component));

        this.key = key;
        this.component = component;
        this.saveDirectoryData = saveDirectoryData;

        this.xSize = ContainerEditInventoryGenItems.SEGMENT_WIDTH * ContainerEditInventoryGenItems.ITEM_COLUMNS + 20;
        this.ySize = 219;
        ((ContainerEditInventoryGenItems) inventorySlots).inventory.addWatcher(this);
    }

    @Override
    public void initGui()
    {
        super.initGui();

        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        weightSliders.clear();
        minMaxSliders.clear();

        int leftEdge = width / 2 - xSize / 2;
        int topEdge = height / 2 - ySize / 2;
        int shiftRightPage = leftEdge + ContainerEditInventoryGenItems.ITEM_COLUMNS * ContainerEditInventoryGenItems.SEGMENT_WIDTH;

        this.buttonList.add(this.backBtn = new GuiButton(0, leftEdge, this.height / 2 + 90, xSize / 3 - 1, 20, IvTranslations.get("gui.back")));

        GuiButton saveButton = new GuiButton(1, leftEdge + xSize / 3 + 1, this.height / 2 + 90, xSize / 3 - 2, 20, TextFormatting.GREEN + IvTranslations.get("reccomplex.gui.save"));
        saveButton.enabled = false;
        this.buttonList.add(saveButton);

        GuiButton hideButton = new GuiButton(2, leftEdge + xSize / 3 * 2 + 1, this.height / 2 + 90, xSize / 3 - 1, 20, IvTranslations.get("reccomplex.gui.hidegui"));
        hideButton.enabled = false; // Can't yet with container gui
        this.buttonList.add(hideButton);

        this.buttonList.add(this.nextPageBtn = new GuiButton(10, shiftRightPage, this.height / 2 - 80, 20, 20, ">"));
        this.buttonList.add(this.prevPageBtn = new GuiButton(11, shiftRightPage, this.height / 2 - 50, 20, 20, "<"));

        for (int col = 0; col < ContainerEditInventoryGenItems.ITEM_COLUMNS; ++col)
        {
            for (int row = 0; row < ContainerEditInventoryGenItems.ITEM_ROWS; ++row)
            {
                int availableSize = ContainerEditInventoryGenItems.SEGMENT_WIDTH - 22 - 4;
                int baseX = leftEdge + 20 + col * ContainerEditInventoryGenItems.SEGMENT_WIDTH;
                int onePart = availableSize / 5;

                GuiSliderRange minMaxSlider = new GuiSliderRange(100, baseX, topEdge + 18 + row * 18, onePart * 2 - 2, 18, "");
                minMaxSlider.addListener(slider -> {
                    List<GenericItemCollection.RandomizedItemStack> chestContents = component.items;
                    if (slider.id < 300)
                    {
                        int stackIndex = slider.id - 200;

                        if (stackIndex < chestContents.size())
                        {
                            GenericItemCollection.RandomizedItemStack chestContent = chestContents.get(stackIndex);
                            IntegerRange intRange = Ranges.roundedIntRange(minMaxSlider.getRange());
                            chestContent.min = intRange.getMin();
                            chestContent.max = intRange.getMax();

                            minMaxSlider.setRange(new FloatRange(chestContent.min, chestContent.max));

                            updateAllItemSliders();
                        }
                    }
                });
                minMaxSlider.setMinValue(1);
                this.buttonList.add(minMaxSlider);
                minMaxSliders.add(minMaxSlider);

                GuiSlider weightSlider = new GuiSlider(200, baseX + onePart * 2, topEdge + 18 + row * 18, onePart * 3, 18, IvTranslations.get("reccomplex.gui.random.weight"));
                weightSlider.addListener(slider -> {
                    List<GenericItemCollection.RandomizedItemStack> chestContents = component.items;
                    if (slider.id < 200 && slider.id >= 100)
                    {
                        int stackIndex = slider.id - 100;
                        if (stackIndex < chestContents.size())
                        {
                            chestContents.get(stackIndex).weight = slider.getValue();
                            updateAllItemSliders();
                        }
                    }
                });
                weightSlider.setMinValue(0);
                weightSlider.setMaxValue(10);
                this.buttonList.add(weightSlider);
                weightSliders.add(weightSlider);
            }
        }

        this.scrollTo(currentColShift);
    }

    public void scrollTo(int colShift)
    {
        currentColShift = colShift;
        ((ContainerEditInventoryGenItems) inventorySlots).scrollTo(colShift);

        updateAllItemSliders();
        updatePageButtons();
    }

    private void updateAllItemSliders()
    {
        List<GenericItemCollection.RandomizedItemStack> chestContents = component.items;

        for (int i = 0; i < weightSliders.size(); i++)
        {
            GuiSlider weightSlider = weightSliders.get(i);
            GuiSliderRange minMaxSlider = minMaxSliders.get(i);

            int index = i + currentColShift * ContainerEditInventoryGenItems.ITEM_ROWS;
            weightSlider.id = index + 100;
            minMaxSlider.id = index + 200;
            if (index < chestContents.size())
            {
                GenericItemCollection.RandomizedItemStack chestContent = chestContents.get(index);
                minMaxSlider.setRange(new FloatRange(chestContent.min, chestContent.max));
                minMaxSlider.setMaxValue(chestContent.itemStack.getMaxStackSize());
                minMaxSlider.enabled = true;
                minMaxSlider.displayString = IvTranslations.format("reccomplex.gui.inventorygen.minMax", chestContent.min, chestContent.max);

                weightSlider.setValue((float) chestContent.weight);
                weightSlider.enabled = true;
                weightSlider.displayString = IvTranslations.format("reccomplex.gui.inventorygen.weightNumber", String.format("%.2f", weightSlider.getValue()));
            }
            else
            {
                minMaxSlider.setRange(new FloatRange(1, 1));
                minMaxSlider.setMaxValue(64);
                minMaxSlider.enabled = false;
                minMaxSlider.displayString = IvTranslations.get("reccomplex.gui.inventorygen.range");

                weightSlider.setValue(weightSlider.getMinValue());
                weightSlider.enabled = false;
                weightSlider.displayString = IvTranslations.format("reccomplex.gui.random.weight");
            }
        }
    }

    private void updatePageButtons()
    {
        List<GenericItemCollection.RandomizedItemStack> chestContents = component.items;
        int neededCols = chestContents.size() / ContainerEditInventoryGenItems.ITEM_ROWS + 1;
        nextPageBtn.enabled = (currentColShift + ContainerEditInventoryGenItems.ITEM_COLUMNS) < neededCols;
        prevPageBtn.enabled = currentColShift > 0;
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        if (button.enabled)
        {
            if (button.id == 0)
            {
                RCGuiHandler.editInventoryGenComponent(mc.player, key, component, saveDirectoryData);
            }
            else if (button.id == 2)
            {
                GuiHider.hideGUI();
            }
            else if (button.id == 10)
            {
                scrollTo(currentColShift + 1);
                RecurrentComplex.network.sendToServer(PacketGuiAction.packetGuiAction("igSelectCol", currentColShift));
            }
            else if (button.id == 11)
            {
                scrollTo(currentColShift - 1);
                RecurrentComplex.network.sendToServer(PacketGuiAction.packetGuiAction("igSelectCol", currentColShift));
            }
        }
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) throws IOException
    {
        if (keyCode == this.mc.gameSettings.keyBindInventory.getKeyCode())
            return;

        if (keyCode == Keyboard.KEY_ESCAPE) // Would otherwise close GUI
        {
            if (backBtn != null)
                actionPerformed(backBtn);

            return;
        }

        super.keyTyped(keyChar, keyCode);

        if (keyCode == Keyboard.KEY_LEFT && prevPageBtn.enabled)
        {
            actionPerformed(prevPageBtn);
        }
        else if (keyCode == Keyboard.KEY_RIGHT && nextPageBtn.enabled)
        {
            actionPerformed(nextPageBtn);
        }
    }

    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();

        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return true;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int mouseX, int mouseY)
    {
        mc.getTextureManager().bindTexture(textureBackground);
        GlStateManager.color(1.0f, 1.0f, 1.0f);
        drawTexturedModalRect(width / 2 - 176 / 2 - 20 / 2 - 1, height / 2 - 13, 0, 0, 176, 90);

        for (int i = 0; i < ContainerEditInventoryGenItems.ITEM_ROWS; i++)
        {
            drawTexturedModalRect(width / 2 - ContainerEditInventoryGenItems.SEGMENT_WIDTH / 2 - 11, height / 2 - 91 + i * 18, 7, 7, 18, 18);
        }
//        for (int i = 0; i < ContainerEditInventoryGen.ITEM_COLUMNS; i++)
//        {
//            int baseX = width / 2 + i * ContainerEditInventoryGen.SEGMENT_WIDTH;
//            drawCenteredString(fontRendererObj, IvTranslations.format("reccomplex.gui.inventorygen.min"), baseX + 20, this.height / 2 - 75, 0xffffffff);
//            drawCenteredString(fontRendererObj, IvTranslations.format("reccomplex.gui.inventorygen.max"), baseX + 40, this.height / 2 - 75, 0xffffffff);
//            drawCenteredString(fontRendererObj, IvTranslations.format("reccomplex.gui.inventorygen.weight"), baseX + 60, this.height / 2 - 75, 0xffffffff);
//        }
    }

    private void drawPlaceholderString(GuiTextField textField, String string)
    {
        if (StringUtils.isNullOrEmpty(textField.getText()))
            drawString(fontRendererObj, string, textField.xPosition + 5, textField.yPosition + 7, 0xff888888);
    }

    @Override
    public void inventoryChanged(IInventory inventory)
    {
        updateAllItemSliders();
        updatePageButtons();
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);

        if (state == 0)
        {
            for (Object object : buttonList)
            {
                GuiButton button = (GuiButton) object;
                button.mouseReleased(mouseX, mouseY);
            }
        }
    }

//    public void updateSaveButtonEnabled()
//    {
//        backBtn.enabled = key.trim().length() > 0 && component.inventoryGeneratorID.trim().length() > 0;
//    }
}
